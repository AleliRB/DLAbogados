package com.dlabogados.seguimiento.service;

import com.dlabogados.caso.entity.Caso;
import com.dlabogados.caso.repository.CasoRepository;
import com.dlabogados.exception.ResourceNotFoundException;
import com.dlabogados.seguimiento.dto.SeguimientoRequest;
import com.dlabogados.seguimiento.dto.SeguimientoResponse;
import com.dlabogados.seguimiento.entity.SeguimientoCaso;
import com.dlabogados.seguimiento.repository.SeguimientoCasoRepository;
import com.dlabogados.usuario.entity.Usuario;
import com.dlabogados.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeguimientoServiceImpl implements SeguimientoService {

    private final SeguimientoCasoRepository seguimientoRepository;
    private final CasoRepository casoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SeguimientoResponse> obtenerPorCaso(Long casoId, boolean esCliente) {
        List<SeguimientoCaso> lista;
        if (esCliente) {
            lista = seguimientoRepository.findByCasoIdAndVisibleClienteTrueOrderByFechaDesc(casoId);
        } else {
            lista = seguimientoRepository.findByCasoIdOrderByFechaDesc(casoId);
        }
        return lista.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SeguimientoResponse registrarSeguimiento(Long casoId, String userEmail, SeguimientoRequest request) {
        Caso caso = casoRepository.findById(casoId)
                .orElseThrow(() -> new ResourceNotFoundException("Caso no encontrado con id: " + casoId));

        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        SeguimientoCaso seg = SeguimientoCaso.builder()
                .caso(caso)
                .usuario(usuario)
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .estadoAnterior(request.getEstadoAnterior())
                .estadoNuevo(request.getEstadoNuevo())
                .visibleCliente(request.getVisibleCliente() != null ? request.getVisibleCliente() : true)
                .build();

        SeguimientoCaso guardado = seguimientoRepository.save(seg);
        return mapToResponse(guardado);
    }

    private SeguimientoResponse mapToResponse(SeguimientoCaso s) {
        return SeguimientoResponse.builder()
                .id(s.getId())
                .casoId(s.getCaso().getId())
                .casoCodigo(s.getCaso().getCodigo())
                .usuarioId(s.getUsuario().getId())
                .usuarioEmail(s.getUsuario().getEmail())
                .titulo(s.getTitulo())
                .descripcion(s.getDescripcion())
                .estadoAnterior(s.getEstadoAnterior())
                .estadoNuevo(s.getEstadoNuevo())
                .visibleCliente(s.getVisibleCliente())
                .fecha(s.getFecha())
                .build();
    }
}
