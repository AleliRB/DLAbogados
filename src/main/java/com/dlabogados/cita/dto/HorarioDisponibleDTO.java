package com.dlabogados.cita.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HorarioDisponibleDTO {
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String horaInicioFormato;
    private String horaFinFormato;
    private String etiqueta;
}
