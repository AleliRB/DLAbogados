package com.dlabogados.caso.dto;

import com.dlabogados.caso.entity.PrioridadCaso;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CasoUpdateRequest {
    private String titulo;
    private String descripcion;
    private PrioridadCaso prioridad;
    private BigDecimal montoAcordado;
    private Long servicioId;
}
