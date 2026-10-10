package com.dlabogados.notificacion.service;

import com.dlabogados.notificacion.dto.NotificacionResponse;
import com.dlabogados.notificacion.entity.TipoNotificacion;

import java.util.List;

public interface NotificacionService {
    List<NotificacionResponse> obtenerPorUsuario(String userEmail);
    List<NotificacionResponse> obtenerNoLeidasPorUsuario(String userEmail);
    void crearNotificacion(Long usuarioId, String titulo, String mensaje, TipoNotificacion tipo);
    void marcarComoLeida(Long id);
    void marcarTodasComoLeidas(String userEmail);
}
