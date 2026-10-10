package com.dlabogados.cliente.service;

import com.dlabogados.cliente.dto.ClienteRequest;
import com.dlabogados.cliente.dto.ClienteResponse;
import com.dlabogados.cliente.entity.Cliente;
import com.dlabogados.cliente.repository.ClienteRepository;
import com.dlabogados.exception.ResourceNotFoundException;
import com.dlabogados.usuario.entity.Usuario;
import com.dlabogados.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> obtenerTodos() {
        return clienteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorUsuarioId(Long usuarioId) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para usuario id: " + usuarioId));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        if (request.getNombres() != null) cliente.setNombres(request.getNombres());
        if (request.getApellidos() != null) cliente.setApellidos(request.getApellidos());
        if (request.getTelefono() != null) cliente.setTelefono(request.getTelefono());
        if (request.getDireccion() != null) cliente.setDireccion(request.getDireccion());

        if (request.getActivo() != null) {
            cliente.getUsuario().setActivo(request.getActivo());
        }

        Cliente clienteActualizado = clienteRepository.save(cliente);
        return mapToResponse(clienteActualizado);
    }

    @Override
    @Transactional
    public void cambiarEstadoActivo(Long id, Boolean activo) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        Usuario usuario = cliente.getUsuario();
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    private ClienteResponse mapToResponse(Cliente c) {
        return ClienteResponse.builder()
                .id(c.getId())
                .usuarioId(c.getUsuario().getId())
                .email(c.getUsuario().getEmail())
                .nombres(c.getNombres())
                .apellidos(c.getApellidos())
                .dni(c.getDni())
                .telefono(c.getTelefono())
                .direccion(c.getDireccion())
                .activo(c.getUsuario().getActivo())
                .fechaRegistro(c.getFechaRegistro())
                .build();
    }
}
