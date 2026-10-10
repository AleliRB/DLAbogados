package com.dlabogados.cita.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaOcupadaDTO {
    private LocalTime horaInicio;
    private LocalTime horaFin;
}
