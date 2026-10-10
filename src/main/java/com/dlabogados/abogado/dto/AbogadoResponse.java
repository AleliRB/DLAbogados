package com.dlabogados.abogado.dto;

import com.dlabogados.especialidad.dto.EspecialidadDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbogadoResponse {
    private Long id;
    private Long usuarioId;
    private String email;
    private String nombres;
    private String apellidos;
    private String dni;
    private String telefono;
    private String numeroColegiatura;
    private String perfilProfesional;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
    private Set<EspecialidadDTO> especialidades;
}
