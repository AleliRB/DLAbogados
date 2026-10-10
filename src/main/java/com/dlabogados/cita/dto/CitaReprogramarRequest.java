package com.dlabogados.cita.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CitaReprogramarRequest {

    @NotNull(message = "La nueva fecha es obligatoria")
    @FutureOrPresent(message = "La fecha debe ser presente o futura")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora de inicio es obligatoria")
    private LocalTime nuevaHoraInicio;

    @NotNull(message = "La nueva hora de fin es obligatoria")
    private LocalTime nuevaHoraFin;

    private String observaciones;
}
