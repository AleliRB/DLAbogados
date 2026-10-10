package com.dlabogados.servicio.service;

import com.dlabogados.especialidad.entity.Especialidad;
import com.dlabogados.especialidad.repository.EspecialidadRepository;
import com.dlabogados.exception.ResourceNotFoundException;
import com.dlabogados.servicio.dto.ServicioLegalDTO;
import com.dlabogados.servicio.entity.ServicioLegal;
import com.dlabogados.servicio.repository.ServicioLegalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioLegalServiceImpl implements ServicioLegalService {

    private final ServicioLegalRepository servicioLegalRepository;
    private final EspecialidadRepository especialidadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ServicioLegalDTO> obtenerTodos() {
        return servicioLegalRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioLegalDTO> obtenerActivos() {
        return servicioLegalRepository.findByActivoTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioLegalDTO> obtenerPorEspecialidad(Long especialidadId) {
        return servicioLegalRepository.findByEspecialidadIdAndActivoTrue(especialidadId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioLegalDTO obtenerPorId(Long id) {
        ServicioLegal s = servicioLegalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio legal no encontrado con id: " + id));
        return mapToDTO(s);
    }

    @Override
    @Transactional
    public ServicioLegalDTO crear(ServicioLegalDTO dto) {
        Especialidad esp = especialidadRepository.findById(dto.getEspecialidadId())
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada con id: " + dto.getEspecialidadId()));

        ServicioLegal s = ServicioLegal.builder()
                .especialidad(esp)
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precioReferencial(dto.getPrecioReferencial())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        ServicioLegal guardado = servicioLegalRepository.save(s);
        return mapToDTO(guardado);
    }

    @Override
    @Transactional
    public ServicioLegalDTO actualizar(Long id, ServicioLegalDTO dto) {
        ServicioLegal s = servicioLegalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio legal no encontrado con id: " + id));

        if (dto.getEspecialidadId() != null) {
            Especialidad esp = especialidadRepository.findById(dto.getEspecialidadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada con id: " + dto.getEspecialidadId()));
            s.setEspecialidad(esp);
        }

        if (dto.getNombre() != null) s.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) s.setDescripcion(dto.getDescripcion());
        if (dto.getPrecioReferencial() != null) s.setPrecioReferencial(dto.getPrecioReferencial());
        if (dto.getActivo() != null) s.setActivo(dto.getActivo());

        ServicioLegal actualizado = servicioLegalRepository.save(s);
        return mapToDTO(actualizado);
    }

    private ServicioLegalDTO mapToDTO(ServicioLegal s) {
        return ServicioLegalDTO.builder()
                .id(s.getId())
                .especialidadId(s.getEspecialidad().getId())
                .especialidadNombre(s.getEspecialidad().getNombre())
                .nombre(s.getNombre())
                .descripcion(s.getDescripcion())
                .precioReferencial(s.getPrecioReferencial())
                .activo(s.getActivo())
                .build();
    }
}
