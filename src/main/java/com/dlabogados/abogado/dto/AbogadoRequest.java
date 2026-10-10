package com.dlabogados.abogado.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Set;

@Data
public class AbogadoRequest {

    @Email(message = "Debe ser un email válido")
    private String email;

    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombres;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener 8 dígitos numéricos")
    private String dni;

    private String telefono;

    @NotBlank(message = "El número de colegiatura es obligatorio")
    private String numeroColegiatura;

    private String perfilProfesional;
    private Boolean activo;
    private Set<Long> especialidadIds;
}
