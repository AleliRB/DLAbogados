package com.dlabogados.caso.service;

import com.dlabogados.caso.dto.*;
import com.dlabogados.caso.entity.EstadoCaso;

import java.util.List;

public interface CasoService {
    List<CasoResponse> obtenerTodos();
    List<CasoResponse> obtenerPorCliente(Long clienteId);
    List<CasoResponse> obtenerPorAbogado(Long abogadoId);
    List<CasoResponse> obtenerPorEstado(EstadoCaso estado);
    CasoResponse obtenerPorId(Long id);
    CasoResponse crear(CasoCreateRequest request);
    CasoResponse actualizar(Long id, CasoUpdateRequest request);
    CasoResponse cambiarEstado(Long id, CambiarEstadoCasoRequest request);
    CasoResponse asignarAbogado(Long casoId, AsignarAbogadoRequest request);
    CasoResponse desasignarAbogado(Long casoId, Long abogadoId);
}
