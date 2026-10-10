package com.dlabogados.reapertura.service;

import com.dlabogados.abogado.entity.Abogado;
import com.dlabogados.abogado.repository.AbogadoRepository;
import com.dlabogados.caso.dto.AsignarAbogadoRequest;
import com.dlabogados.caso.dto.CasoCreateRequest;
import com.dlabogados.caso.dto.CasoResponse;
import com.dlabogados.caso.entity.Caso;
import com.dlabogados.caso.entity.CasoAbogado;
import com.dlabogados.caso.entity.EstadoCaso;
import com.dlabogados.caso.repository.CasoAbogadoRepository;
import com.dlabogados.caso.repository.CasoRepository;
import com.dlabogados.caso.service.CasoService;
import com.dlabogados.exception.BadRequestException;
import com.dlabogados.exception.ResourceNotFoundException;
import com.dlabogados.reapertura.dto.ReaperturaRequest;
import com.dlabogados.reapertura.dto.ReaperturaRespuestaRequest;
import com.dlabogados.reapertura.dto.ReaperturaResponse;
import com.dlabogados.reapertura.entity.EstadoReapertura;
import com.dlabogados.reapertura.entity.Reapertura;
import com.dlabogados.reapertura.repository.ReaperturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReaperturaServiceImpl implements ReaperturaService {

    private final ReaperturaRepository reaperturaRepository;
    private final CasoRepository casoRepository;
    private final CasoAbogadoRepository casoAbogadoRepository;
    private final AbogadoRepository abogadoRepository;
    private final CasoService casoService;
    private final com.dlabogados.historial.service.HistorialActividadService historialActividadService;

    @Override
    @Transactional(readOnly = true)
    public List<ReaperturaResponse> obtenerTodas() {
        return reaperturaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReaperturaResponse> obtenerPorCliente(Long clienteId) {
        return reaperturaRepository.findByClienteId(clienteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReaperturaResponse> obtenerPorAbogadoPreferido(Long abogadoId) {
        return reaperturaRepository.findByAbogadoPreferidoId(abogadoId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReaperturaResponse obtenerPorId(Long id) {
        Reapertura r = reaperturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de reapertura no encontrada con id: " + id));
        return mapToResponse(r);
    }

    @Override
    @Transactional
    public ReaperturaResponse solicitarReapertura(Long casoId, ReaperturaRequest request) {
        Caso casoAnterior = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado con id: " + casoId));

        if (casoAnterior.getEstado() != EstadoCaso.FINALIZADO && casoAnterior.getEstado() != EstadoCaso.CERRADO) {
            throw new BadRequestException("Solo se pueden solicitar reaperturas sobre casos en estado FINALIZADO o CERRADO");
        }

        Abogado abogadoPreferido = null;
        if (request.getAbogadoPreferidoId() != null) {
            abogadoPreferido = abogadoRepository.findById(request.getAbogadoPreferidoId()).orElse(null);
        } else {
            // Intentar recuperar el abogado responsable anterior
            Optional<CasoAbogado> respPrevio = casoAbogadoRepository.findByCasoIdAndEsResponsableTrueAndActivoTrue(casoId);
            if (respPrevio.isPresent()) {
                abogadoPreferido = respPrevio.get().getAbogado();
            }
        }

        Reapertura r = Reapertura.builder()
                .casoAnterior(casoAnterior)
                .cliente(casoAnterior.getCliente())
                .abogadoPreferido(abogadoPreferido)
                .motivo(request.getMotivo())
                .estado(EstadoReapertura.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now())
                .build();

        Reapertura guardada = reaperturaRepository.save(r);

        historialActividadService.registrarActividad(
                casoAnterior.getCliente() != null && casoAnterior.getCliente().getUsuario() != null ? casoAnterior.getCliente().getUsuario().getId() : null,
                "REAPERTURA",
                guardada.getId(),
                "SOLICITUD_REAPERTURA",
                "Solicitud de reapertura registrada para el caso cerrado " + casoAnterior.getCodigo() + ". Motivo: " + request.getMotivo()
        );

        return mapToResponse(guardada);
    }

    @Override
    @Transactional
    public ReaperturaResponse aprobarReapertura(Long id, ReaperturaRespuestaRequest request) {
        Reapertura r = reaperturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de reapertura no encontrada con id: " + id));

        if (r.getEstado() != EstadoReapertura.PENDIENTE) {
            throw new BadRequestException("La solicitud ya ha sido procesada previamente");
        }

        Caso casoAnterior = r.getCasoAnterior();

        // 1. Crear nuevo caso conservando cliente, servicio legal y enlace a casoOrigen
        CasoCreateRequest nuevoCasoReq = new CasoCreateRequest();
        nuevoCasoReq.setClienteId(casoAnterior.getCliente().getId());
        nuevoCasoReq.setServicioId(casoAnterior.getServicioLegal().getId());
        nuevoCasoReq.setCasoOrigenId(casoAnterior.getId());
        nuevoCasoReq.setTitulo("REAPERTURA: " + casoAnterior.getTitulo());
        nuevoCasoReq.setDescripcion("Caso reaperturado por motivo: " + r.getMotivo());
        nuevoCasoReq.setPrioridad(casoAnterior.getPrioridad());
        nuevoCasoReq.setMontoAcordado(casoAnterior.getMontoAcordado());

        // Definir abogado asignado (solicitado en request o preferido/anterior)
        Long abogadoFinalId = null;
        if (request != null && request.getAbogadoAsignadoId() != null) {
            abogadoFinalId = request.getAbogadoAsignadoId();
        } else if (r.getAbogadoPreferido() != null && Boolean.TRUE.equals(r.getAbogadoPreferido().getActivo())) {
            abogadoFinalId = r.getAbogadoPreferido().getId();
        }

        nuevoCasoReq.setAbogadoResponsableId(abogadoFinalId);

        CasoResponse nuevoCasoDTO = casoService.crear(nuevoCasoReq);
        Caso nuevoCasoEntity = casoRepository.findById(nuevoCasoDTO.getId()).orElseThrow();

        // Actualizar registro de reapertura
        r.setNuevoCaso(nuevoCasoEntity);
        r.setEstado(EstadoReapertura.APROBADA);
        r.setFechaRespuesta(LocalDateTime.now());
        if (request != null && request.getObservacionRespuesta() != null) {
            r.setObservacionRespuesta(request.getObservacionRespuesta());
        }

        Reapertura actualizada = reaperturaRepository.save(r);

        // Registro de auditoría
        historialActividadService.registrarActividad(
                null,
                "REAPERTURA",
                actualizada.getId(),
                "APROBACION_REAPERTURA",
                "Reapertura aprobada. Se generó el nuevo expediente " + nuevoCasoEntity.getCodigo() + " a partir del caso " + casoAnterior.getCodigo()
        );

        return mapToResponse(actualizada);
    }

    @Override
    @Transactional
    public ReaperturaResponse rechazarReapertura(Long id, ReaperturaRespuestaRequest request) {
        Reapertura r = reaperturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de reapertura no encontrada con id: " + id));

        if (r.getEstado() != EstadoReapertura.PENDIENTE) {
            throw new BadRequestException("La solicitud ya ha sido procesada previamente");
        }

        r.setEstado(EstadoReapertura.RECHAZADA);
        r.setFechaRespuesta(LocalDateTime.now());
        if (request != null && request.getObservacionRespuesta() != null) {
            r.setObservacionRespuesta(request.getObservacionRespuesta());
        }

        Reapertura actualizada = reaperturaRepository.save(r);

        // Registro de auditoría
        historialActividadService.registrarActividad(
                null,
                "REAPERTURA",
                actualizada.getId(),
                "RECHAZO_REAPERTURA",
                "Reapertura rechazada para el caso " + r.getCasoAnterior().getCodigo() + (request != null && request.getObservacionRespuesta() != null ? ". Motivo: " + request.getObservacionRespuesta() : "")
        );

        return mapToResponse(actualizada);
    }

    private ReaperturaResponse mapToResponse(Reapertura r) {
        return ReaperturaResponse.builder()
                .id(r.getId())
                .casoAnteriorId(r.getCasoAnterior().getId())
                .casoAnteriorCodigo(r.getCasoAnterior().getCodigo())
                .casoAnteriorTitulo(r.getCasoAnterior().getTitulo())
                .clienteId(r.getCliente().getId())
                .clienteNombre(r.getCliente().getNombres() + " " + r.getCliente().getApellidos())
                .abogadoPreferidoId(r.getAbogadoPreferido() != null ? r.getAbogadoPreferido().getId() : null)
                .abogadoPreferidoNombre(r.getAbogadoPreferido() != null ? r.getAbogadoPreferido().getNombres() + " " + r.getAbogadoPreferido().getApellidos() : null)
                .nuevoCasoId(r.getNuevoCaso() != null ? r.getNuevoCaso().getId() : null)
                .nuevoCasoCodigo(r.getNuevoCaso() != null ? r.getNuevoCaso().getCodigo() : null)
                .motivo(r.getMotivo())
                .estado(r.getEstado())
                .observacionRespuesta(r.getObservacionRespuesta())
                .fechaSolicitud(r.getFechaSolicitud())
                .fechaRespuesta(r.getFechaRespuesta())
                .build();
    }
}
