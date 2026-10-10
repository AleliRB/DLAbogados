package com.dlabogados.cliente.service;

import com.dlabogados.cliente.dto.ClienteRequest;
import com.dlabogados.cliente.dto.ClienteResponse;

import java.util.List;

public interface ClienteService {
    List<ClienteResponse> obtenerTodos();
    ClienteResponse obtenerPorId(Long id);
    ClienteResponse obtenerPorUsuarioId(Long usuarioId);
    ClienteResponse actualizar(Long id, ClienteRequest request);
    void cambiarEstadoActivo(Long id, Boolean activo);
}
