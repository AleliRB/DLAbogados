package com.dlabogados.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String rol;
    private String nombres;
    private String apellidos;
    private String dni;
    private Long clienteId;
    private Long abogadoId;
}
