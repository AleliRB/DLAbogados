package com.dlabogados.cita.dto;

import com.dlabogados.cita.entity.EstadoCita;
import com.dlabogados.cita.entity.ModalidadCita;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaResponse {
    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private Long abogadoId;
    private String abogadoNombre;
    private Long servicioId;
    private String servicioNombre;
    private Long casoId;
    private String casoCodigo;
    private String casoTitulo;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private ModalidadCita modalidad;
    private String motivo;
    private EstadoCita estado;
    private String enlaceVirtual;
    private String observaciones;
    private LocalDateTime fechaCreacion;
}
