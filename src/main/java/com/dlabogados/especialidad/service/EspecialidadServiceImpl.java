package com.dlabogados.especialidad.service;

import com.dlabogados.especialidad.dto.EspecialidadDTO;
import com.dlabogados.especialidad.entity.Especialidad;
import com.dlabogados.especialidad.repository.EspecialidadRepository;
import com.dlabogados.exception.ConflictException;
import com.dlabogados.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadDTO> obtenerTodas() {
        return especialidadRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadDTO> obtenerActivas() {
        return especialidadRepository.findByActivoTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EspecialidadDTO obtenerPorId(Long id) {
        Especialidad e = especialidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada con id: " + id));
        return mapToDTO(e);
    }

    @Override
    @Transactional
    public EspecialidadDTO crear(EspecialidadDTO dto) {
        if (especialidadRepository.existsByNombre(dto.getNombre())) {
            throw new ConflictException("Ya existe una especialidad con el nombre: " + dto.getNombre());
        }
        Especialidad e = Especialidad.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        Especialidad guardada = especialidadRepository.save(e);
        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public EspecialidadDTO actualizar(Long id, EspecialidadDTO dto) {
        Especialidad e = especialidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada con id: " + id));

        if (dto.getNombre() != null && !dto.getNombre().equalsIgnoreCase(e.getNombre())) {
            if (especialidadRepository.existsByNombre(dto.getNombre())) {
                throw new ConflictException("Ya existe otra especialidad con el nombre: " + dto.getNombre());
            }
            e.setNombre(dto.getNombre());
        }

        if (dto.getDescripcion() != null) e.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) e.setActivo(dto.getActivo());

        Especialidad actualizada = especialidadRepository.save(e);
        return mapToDTO(actualizada);
    }

    private EspecialidadDTO mapToDTO(Especialidad e) {
        return EspecialidadDTO.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .descripcion(e.getDescripcion())
                .activo(e.getActivo())
                .build();
    }
}
