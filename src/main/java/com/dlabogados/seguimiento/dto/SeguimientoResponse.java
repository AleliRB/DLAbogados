package com.dlabogados.seguimiento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguimientoResponse {
    private Long id;
    private Long casoId;
    private String casoCodigo;
    private Long usuarioId;
    private String usuarioEmail;
    private String titulo;
    private String descripcion;
    private String estadoAnterior;
    private String estadoNuevo;
    private Boolean visibleCliente;
    private LocalDateTime fecha;
}
