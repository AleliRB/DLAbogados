package com.dlabogados.cliente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponse {
    private Long id;
    private Long usuarioId;
    private String email;
    private String nombres;
    private String apellidos;
    private String dni;
    private String telefono;
    private String direccion;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
}
