package com.dlabogados.caso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignarAbogadoRequest {

    @NotNull(message = "El ID del abogado es obligatorio")
    private Long abogadoId;

    private Boolean esResponsable = true;
    private String observacion;
}
