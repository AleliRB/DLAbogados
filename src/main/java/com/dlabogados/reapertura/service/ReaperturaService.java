package com.dlabogados.reapertura.service;

import com.dlabogados.reapertura.dto.ReaperturaRequest;
import com.dlabogados.reapertura.dto.ReaperturaRespuestaRequest;
import com.dlabogados.reapertura.dto.ReaperturaResponse;

import java.util.List;

public interface ReaperturaService {
    List<ReaperturaResponse> obtenerTodas();
    List<ReaperturaResponse> obtenerPorCliente(Long clienteId);
    List<ReaperturaResponse> obtenerPorAbogadoPreferido(Long abogadoId);
    ReaperturaResponse obtenerPorId(Long id);
    ReaperturaResponse solicitarReapertura(Long casoId, ReaperturaRequest request);
    ReaperturaResponse aprobarReapertura(Long id, ReaperturaRespuestaRequest request);
    ReaperturaResponse rechazarReapertura(Long id, ReaperturaRespuestaRequest request);
}
