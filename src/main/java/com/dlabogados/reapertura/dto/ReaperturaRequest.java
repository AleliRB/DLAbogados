package com.dlabogados.reapertura.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReaperturaRequest {

    @NotBlank(message = "El motivo de reapertura es obligatorio")
    private String motivo;

    private Long abogadoPreferidoId;
}
