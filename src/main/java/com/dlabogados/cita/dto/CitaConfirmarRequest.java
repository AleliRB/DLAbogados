package com.dlabogados.cita.dto;

import lombok.Data;

@Data
public class CitaConfirmarRequest {
    private String enlaceVirtual;
    private String observaciones;
}
