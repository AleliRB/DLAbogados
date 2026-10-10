package com.dlabogados.servicio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioLegalDTO {
    private Long id;

    @NotNull(message = "La especialidad es obligatoria")
    private Long especialidadId;

    private String especialidadNombre;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    private String nombre;

    private String descripcion;
    private BigDecimal precioReferencial;
    private Boolean activo;
}
