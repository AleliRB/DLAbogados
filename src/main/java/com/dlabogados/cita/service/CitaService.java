package com.dlabogados.cita.service;

import com.dlabogados.cita.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface CitaService {
    List<CitaResponse> obtenerTodas();
    List<CitaResponse> obtenerPorCliente(Long clienteId);
    List<CitaResponse> obtenerPorAbogado(Long abogadoId);
    List<CitaResponse> obtenerPorCaso(Long casoId);
    List<CitaOcupadaDTO> obtenerOcupadas(Long abogadoId, LocalDate fecha);
    List<HorarioDisponibleDTO> obtenerHorariosDisponibles(Long abogadoId, LocalDate fecha, Long servicioId);
    CitaResponse obtenerPorId(Long id);
    CitaResponse crear(CitaCreateRequest request);
    CitaResponse confirmar(Long id, CitaConfirmarRequest request);
    CitaResponse atender(Long id, String observaciones);
    CitaResponse cancelar(Long id, String motivo);
    CitaResponse reprogramar(Long id, CitaReprogramarRequest request);
}
