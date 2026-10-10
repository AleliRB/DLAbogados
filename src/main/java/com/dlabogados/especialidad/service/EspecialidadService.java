package com.dlabogados.especialidad.service;

import com.dlabogados.especialidad.dto.EspecialidadDTO;

import java.util.List;

public interface EspecialidadService {
    List<EspecialidadDTO> obtenerTodas();
    List<EspecialidadDTO> obtenerActivas();
    EspecialidadDTO obtenerPorId(Long id);
    EspecialidadDTO crear(EspecialidadDTO dto);
    EspecialidadDTO actualizar(Long id, EspecialidadDTO dto);
}
