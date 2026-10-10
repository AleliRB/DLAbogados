package com.dlabogados.reapertura.dto;

import lombok.Data;

@Data
public class ReaperturaRespuestaRequest {
    private String observacionRespuesta;
    private Long abogadoAsignadoId; // Opcional para sobreescribir el abogado asignado al nuevo caso
}
