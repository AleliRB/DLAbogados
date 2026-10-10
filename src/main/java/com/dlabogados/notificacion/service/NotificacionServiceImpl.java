package com.dlabogados.notificacion.service;

import com.dlabogados.exception.ResourceNotFoundException;
import com.dlabogados.notificacion.dto.NotificacionResponse;
import com.dlabogados.notificacion.entity.Notificacion;
import com.dlabogados.notificacion.entity.TipoNotificacion;
import com.dlabogados.notificacion.repository.NotificacionRepository;
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
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponse> obtenerPorUsuario(String userEmail) {
        Usuario u = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(u.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponse> obtenerNoLeidasPorUsuario(String userEmail) {
        Usuario u = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(u.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void crearNotificacion(Long usuarioId, String titulo, String mensaje, TipoNotificacion tipo) {
        Usuario u = usuarioRepository.findById(usuarioId).orElse(null);
        if (u != null) {
            Notificacion n = Notificacion.builder()
                    .usuario(u)
                    .titulo(titulo)
                    .mensaje(mensaje)
                    .tipo(tipo)
                    .leida(false)
                    .fechaCreacion(LocalDateTime.now())
                    .build();
            notificacionRepository.save(n);
        }
    }

    @Override
    @Transactional
    public void marcarComoLeida(Long id) {
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));
        n.setLeida(true);
        n.setFechaLectura(LocalDateTime.now());
        notificacionRepository.save(n);
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidas(String userEmail) {
        Usuario u = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        List<Notificacion> noLeidas = notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(u.getId());
        noLeidas.forEach(n -> {
            n.setLeida(true);
            n.setFechaLectura(LocalDateTime.now());
        });
        notificacionRepository.saveAll(noLeidas);
    }

    private NotificacionResponse mapToResponse(Notificacion n) {
        return NotificacionResponse.builder()
                .id(n.getId())
                .usuarioId(n.getUsuario().getId())
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .tipo(n.getTipo())
                .leida(n.getLeida())
                .fechaCreacion(n.getFechaCreacion())
                .fechaLectura(n.getFechaLectura())
                .build();
    }
}
