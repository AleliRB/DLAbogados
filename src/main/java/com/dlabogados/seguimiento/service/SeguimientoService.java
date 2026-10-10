package com.dlabogados.seguimiento.service;

import com.dlabogados.seguimiento.dto.SeguimientoRequest;
import com.dlabogados.seguimiento.dto.SeguimientoResponse;

import java.util.List;

public interface SeguimientoService {
    List<SeguimientoResponse> obtenerPorCaso(Long casoId, boolean esCliente);
    SeguimientoResponse registrarSeguimiento(Long casoId, String userEmail, SeguimientoRequest request);
}
