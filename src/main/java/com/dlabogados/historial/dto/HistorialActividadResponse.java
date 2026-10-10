package com.dlabogados.historial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialActividadResponse {
    private Long id;
    private Long usuarioId;
    private String usuarioEmail;
    private String tipoEntidad;
    private Long entidadId;
    private String accion;
    private String descripcion;
    private LocalDateTime fecha;
}
