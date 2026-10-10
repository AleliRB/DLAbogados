package com.dlabogados.notificacion.dto;

import com.dlabogados.notificacion.entity.TipoNotificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionResponse {
    private Long id;
    private Long usuarioId;
    private String titulo;
    private String mensaje;
    private TipoNotificacion tipo;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLectura;
}
