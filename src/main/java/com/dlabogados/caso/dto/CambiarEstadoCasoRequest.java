package com.dlabogados.caso.dto;

import com.dlabogados.caso.entity.EstadoCaso;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambiarEstadoCasoRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoCaso estado;

    private String observacion;
}
