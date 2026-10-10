package com.dlabogados.cliente.dto;

import jakarta.validation.constraints.Pattern;

import lombok.Data;

@Data
public class ClienteRequest {
    private String nombres;
    private String apellidos;

    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener 8 dígitos numéricos")
    private String dni;

    private String telefono;
    private String direccion;
    private Boolean activo;
}
