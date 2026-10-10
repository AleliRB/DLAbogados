package com.dlabogados.servicio.service;

import com.dlabogados.servicio.dto.ServicioLegalDTO;

import java.util.List;

public interface ServicioLegalService {
    List<ServicioLegalDTO> obtenerTodos();
    List<ServicioLegalDTO> obtenerActivos();
    List<ServicioLegalDTO> obtenerPorEspecialidad(Long especialidadId);
    ServicioLegalDTO obtenerPorId(Long id);
    ServicioLegalDTO crear(ServicioLegalDTO dto);
    ServicioLegalDTO actualizar(Long id, ServicioLegalDTO dto);
}
