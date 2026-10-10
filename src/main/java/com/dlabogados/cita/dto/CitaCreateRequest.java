package com.dlabogados.cita.dto;

import com.dlabogados.cita.entity.ModalidadCita;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CitaCreateRequest {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El abogado es obligatorio")
    private Long abogadoId;

    @NotNull(message = "El servicio legal es obligatorio")
    private Long servicioId;

    private Long casoId; // Opcional (null para citas sin expediente previo)

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha debe ser presente o futura")
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    @NotNull(message = "La modalidad es obligatoria")
    private ModalidadCita modalidad;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private String enlaceVirtual;
    private String observaciones;
}
