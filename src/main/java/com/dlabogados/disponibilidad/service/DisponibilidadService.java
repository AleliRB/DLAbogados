package com.dlabogados.disponibilidad.service;

import com.dlabogados.disponibilidad.dto.DisponibilidadDTO;

import java.util.List;

public interface DisponibilidadService {
    List<DisponibilidadDTO> obtenerPorAbogado(Long abogadoId);
    DisponibilidadDTO guardar(DisponibilidadDTO dto);
    void eliminar(Long id);
}
