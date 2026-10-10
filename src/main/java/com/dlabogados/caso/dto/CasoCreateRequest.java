package com.dlabogados.caso.dto;

import com.dlabogados.caso.entity.PrioridadCaso;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CasoCreateRequest {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El servicio legal es obligatorio")
    private Long servicioId;

    private Long abogadoResponsableId;
    private Long casoOrigenId;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    @NotNull(message = "La prioridad es obligatoria")
    private PrioridadCaso prioridad;

    @NotNull(message = "El monto acordado es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto acordado debe ser mayor a cero")
    private BigDecimal montoAcordado;
}
