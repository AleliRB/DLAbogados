package com.dlabogados.historial.service;

import com.dlabogados.historial.dto.HistorialActividadResponse;

import java.util.List;

public interface HistorialActividadService {
    List<HistorialActividadResponse> obtenerTodos();
    List<HistorialActividadResponse> obtenerPorEntidad(String tipoEntidad, Long entidadId);
    List<HistorialActividadResponse> obtenerPorTipo(String tipoEntidad);
    List<HistorialActividadResponse> obtenerPorUsuario(Long usuarioId);
    void registrarActividad(Long usuarioId, String tipoEntidad, Long entidadId, String accion, String descripcion);
    void registrarActividadPorEmail(String userEmail, String tipoEntidad, Long entidadId, String accion, String descripcion);
}
