package com.dlabogados.seguimiento.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SeguimientoRequest {

    @NotBlank(message = "El título del seguimiento es obligatorio")
    private String titulo;

    private String descripcion;
    private String estadoAnterior;
    private String estadoNuevo;
    private Boolean visibleCliente = true;
}
