package com.dlabogados.documento.service;

import com.dlabogados.caso.entity.Caso;
import com.dlabogados.caso.repository.CasoRepository;
import com.dlabogados.documento.dto.DocumentoResponse;
import com.dlabogados.documento.entity.Documento;
import com.dlabogados.documento.entity.EstadoDocumento;
import com.dlabogados.documento.repository.DocumentoRepository;
import com.dlabogados.exception.ForbiddenException;
import com.dlabogados.exception.ResourceNotFoundException;
import com.dlabogados.historial.service.HistorialActividadService;
import com.dlabogados.usuario.entity.Usuario;
import com.dlabogados.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentoServiceImpl implements DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final CasoRepository casoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FileStorageService fileStorageService;
    private final HistorialActividadService historialActividadService;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoResponse> obtenerPorCaso(Long casoId, boolean esCliente, String userEmail) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado con id: " + casoId));

        if (esCliente) {
            if (caso.getCliente() == null || !caso.getCliente().getUsuario().getEmail().equalsIgnoreCase(userEmail)) {
                throw new ForbiddenException("No tiene permisos para acceder a los documentos de este expediente");
            }
            return documentoRepository.findByCasoIdAndVisibleClienteTrueAndActivoTrue(casoId)
                    .stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        return documentoRepository.findByCasoIdAndActivoTrue(casoId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoResponse> obtenerTodos(String estado, Long casoId) {
        List<Documento> docs;
        if (casoId != null && estado != null && !estado.trim().isEmpty()) {
            EstadoDocumento est = EstadoDocumento.valueOf(estado.toUpperCase());
            docs = documentoRepository.findByCasoIdAndEstadoAndActivoTrueOrderByFechaSubidaDesc(casoId, est);
        } else if (estado != null && !estado.trim().isEmpty()) {
            EstadoDocumento est = EstadoDocumento.valueOf(estado.toUpperCase());
            docs = documentoRepository.findByEstadoAndActivoTrueOrderByFechaSubidaDesc(est);
        } else if (casoId != null) {
            docs = documentoRepository.findByCasoIdAndActivoTrue(casoId);
        } else {
            docs = documentoRepository.findAllByActivoTrueOrderByFechaSubidaDesc();
        }
        return docs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoResponse obtenerPorId(Long id) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con id: " + id));
        return mapToResponse(doc);
    }

    @Override
    @Transactional
    public DocumentoResponse subirDocumento(Long casoId, String userEmail, MultipartFile file, String categoria, String descripcion, Boolean visibleCliente) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado con id: " + casoId));

        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        String storedFileName = fileStorageService.storeFile(file);

        Documento doc = Documento.builder()
                .caso(caso)
                .usuario(usuario)
                .nombreOriginal(file.getOriginalFilename())
                .nombreArchivo(storedFileName)
                .rutaArchivo(storedFileName)
                .tipoMime(file.getContentType())
                .categoria(categoria != null ? categoria : "GENERAL")
                .descripcion(descripcion)
                .visibleCliente(visibleCliente != null ? visibleCliente : true)
                .estado(EstadoDocumento.RECIBIDO)
                .version(1)
                .activo(true)
                .build();

        Documento guardado = documentoRepository.save(doc);

        // Registro de auditoría
        historialActividadService.registrarActividad(
                usuario.getId(),
                "DOCUMENTO",
                guardado.getId(),
                "SUBIDA_DOCUMENTO",
                "Documento '" + guardado.getNombreOriginal() + "' subido para el expediente " + caso.getCodigo()
        );

        return mapToResponse(guardado);
    }

    @Override
    @Transactional
    public DocumentoResponse cambiarEstado(Long id, EstadoDocumento nuevoEstado, String observaciones, String userEmail) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con id: " + id));

        Usuario usuario = usuarioRepository.findByEmail(userEmail).orElse(null);

        EstadoDocumento anterior = doc.getEstado();
        doc.setEstado(nuevoEstado);
        if (observaciones != null) {
            doc.setObservaciones(observaciones);
        }

        Documento actualizado = documentoRepository.save(doc);

        // Registro de auditoría
        historialActividadService.registrarActividad(
                usuario != null ? usuario.getId() : null,
                "DOCUMENTO",
                actualizado.getId(),
                "CAMBIO_ESTADO_DOCUMENTO",
                "Documento '" + doc.getNombreOriginal() + "' cambió de estado: " + anterior + " -> " + nuevoEstado +
                        (observaciones != null ? " [Obs: " + observaciones + "]" : "")
        );

        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public DocumentoResponse sustituirDocumento(Long id, MultipartFile file, String userEmail) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con id: " + id));

        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        String storedFileName = fileStorageService.storeFile(file);

        doc.setNombreOriginal(file.getOriginalFilename());
        doc.setNombreArchivo(storedFileName);
        doc.setRutaArchivo(storedFileName);
        doc.setTipoMime(file.getContentType());
        doc.setVersion(doc.getVersion() != null ? doc.getVersion() + 1 : 2);
        doc.setEstado(EstadoDocumento.EN_REVISION);
        doc.setObservaciones("Documento sustituido por el usuario (Versión " + doc.getVersion() + ")");

        Documento actualizado = documentoRepository.save(doc);

        // Registro de auditoría
        historialActividadService.registrarActividad(
                usuario.getId(),
                "DOCUMENTO",
                actualizado.getId(),
                "SUSTITUCION_DOCUMENTO",
                "Documento sustituido con nueva versión " + actualizado.getVersion() + " ('" + file.getOriginalFilename() + "')"
        );

        return mapToResponse(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource descargarDocumento(Long id, boolean esCliente, String userEmail) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con id: " + id));

        if (esCliente) {
            if (doc.getCaso() == null || doc.getCaso().getCliente() == null || 
                !doc.getCaso().getCliente().getUsuario().getEmail().equalsIgnoreCase(userEmail)) {
                throw new ForbiddenException("No tiene permisos para descargar documentos de expedientes ajenos");
            }
            if (Boolean.FALSE.equals(doc.getVisibleCliente())) {
                throw new ForbiddenException("No tiene permisos para acceder a este documento privado");
            }
        }

        return fileStorageService.loadFileAsResource(doc.getNombreArchivo());
    }

    @Override
    @Transactional
    public void eliminarDocumento(Long id) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con id: " + id));
        doc.setActivo(false);
        documentoRepository.save(doc);

        historialActividadService.registrarActividad(
                null,
                "DOCUMENTO",
                id,
                "ELIMINACION_DOCUMENTO",
                "Documento '" + doc.getNombreOriginal() + "' deshabilitado del expediente " + (doc.getCaso() != null ? doc.getCaso().getCodigo() : "N/A")
        );
    }

    private DocumentoResponse mapToResponse(Documento d) {
        return DocumentoResponse.builder()
                .id(d.getId())
                .casoId(d.getCaso().getId())
                .casoCodigo(d.getCaso().getCodigo())
                .usuarioId(d.getUsuario().getId())
                .usuarioEmail(d.getUsuario().getEmail())
                .nombreOriginal(d.getNombreOriginal())
                .nombreArchivo(d.getNombreArchivo())
                .tipoMime(d.getTipoMime())
                .categoria(d.getCategoria())
                .descripcion(d.getDescripcion())
                .visibleCliente(d.getVisibleCliente())
                .fechaSubida(d.getFechaSubida())
                .activo(d.getActivo())
                .estado(d.getEstado())
                .observaciones(d.getObservaciones())
                .version(d.getVersion())
                .build();
    }
}
