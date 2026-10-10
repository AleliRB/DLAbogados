package com.dlabogados.historial.service;

import com.dlabogados.historial.dto.HistorialActividadResponse;
import com.dlabogados.historial.entity.HistorialActividad;
import com.dlabogados.historial.repository.HistorialActividadRepository;
import com.dlabogados.usuario.entity.Usuario;
import com.dlabogados.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistorialActividadServiceImpl implements HistorialActividadService {

    private final HistorialActividadRepository historialRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HistorialActividadResponse> obtenerTodos() {
        return historialRepository.findAllByOrderByFechaDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialActividadResponse> obtenerPorEntidad(String tipoEntidad, Long entidadId) {
        return historialRepository.findByTipoEntidadAndEntidadIdOrderByFechaDesc(tipoEntidad, entidadId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialActividadResponse> obtenerPorTipo(String tipoEntidad) {
        return historialRepository.findByTipoEntidadOrderByFechaDesc(tipoEntidad).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialActividadResponse> obtenerPorUsuario(Long usuarioId) {
        return historialRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void registrarActividad(Long usuarioId, String tipoEntidad, Long entidadId, String accion, String descripcion) {
        Usuario usuario = null;
        if (usuarioId != null) {
            usuario = usuarioRepository.findById(usuarioId).orElse(null);
        }

        HistorialActividad h = HistorialActividad.builder()
                .usuario(usuario)
                .tipoEntidad(tipoEntidad)
                .entidadId(entidadId)
                .accion(accion)
                .descripcion(descripcion)
                .fecha(LocalDateTime.now())
                .build();

        historialRepository.save(h);
    }

    @Override
    @Transactional
    public void registrarActividadPorEmail(String userEmail, String tipoEntidad, Long entidadId, String accion, String descripcion) {
        Usuario usuario = null;
        if (userEmail != null) {
            usuario = usuarioRepository.findByEmail(userEmail).orElse(null);
        }

        HistorialActividad h = HistorialActividad.builder()
                .usuario(usuario)
                .tipoEntidad(tipoEntidad)
                .entidadId(entidadId)
                .accion(accion)
                .descripcion(descripcion)
                .fecha(LocalDateTime.now())
                .build();

        historialRepository.save(h);
    }

    private HistorialActividadResponse mapToResponse(HistorialActividad h) {
        String usuarioNombre = "SISTEMA";
        if (h.getUsuario() != null) {
            usuarioNombre = h.getUsuario().getEmail();
        }

        return HistorialActividadResponse.builder()
                .id(h.getId())
                .usuarioId(h.getUsuario() != null ? h.getUsuario().getId() : null)
                .usuarioEmail(usuarioNombre)
                .tipoEntidad(h.getTipoEntidad())
                .entidadId(h.getEntidadId())
                .accion(h.getAccion())
                .descripcion(h.getDescripcion())
                .fecha(h.getFecha())
                .build();
    }
}
