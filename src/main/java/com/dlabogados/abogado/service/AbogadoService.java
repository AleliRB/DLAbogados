package com.dlabogados.abogado.service;

import com.dlabogados.abogado.dto.AbogadoRequest;
import com.dlabogados.abogado.dto.AbogadoResponse;

import java.util.List;

public interface AbogadoService {
    List<AbogadoResponse> obtenerTodos();
    List<AbogadoResponse> obtenerActivos();
    List<AbogadoResponse> obtenerPorServicio(Long servicioId);
    AbogadoResponse obtenerPorId(Long id);
    AbogadoResponse obtenerPorUsuarioId(Long usuarioId);
    AbogadoResponse registrar(AbogadoRequest request);
    AbogadoResponse actualizar(Long id, AbogadoRequest request);
    void cambiarEstadoActivo(Long id, Boolean activo);
}
