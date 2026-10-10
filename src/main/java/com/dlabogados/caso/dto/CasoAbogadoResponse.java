package com.dlabogados.caso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CasoAbogadoResponse {
    private Long id;
    private Long abogadoId;
    private String abogadoNombre;
    private String abogadoColegiatura;
    private Boolean esResponsable;
    private Boolean activo;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaFin;
    private String observacion;
}
