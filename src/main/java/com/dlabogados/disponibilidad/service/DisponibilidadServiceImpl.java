package com.dlabogados.disponibilidad.service;

import com.dlabogados.abogado.entity.Abogado;
import com.dlabogados.abogado.repository.AbogadoRepository;
import com.dlabogados.disponibilidad.dto.DisponibilidadDTO;
import com.dlabogados.disponibilidad.entity.DisponibilidadAbogado;
import com.dlabogados.disponibilidad.repository.DisponibilidadAbogadoRepository;
import com.dlabogados.exception.BadRequestException;
import com.dlabogados.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisponibilidadServiceImpl implements DisponibilidadService {

    private final DisponibilidadAbogadoRepository disponibilidadRepository;
    private final AbogadoRepository abogadoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadDTO> obtenerPorAbogado(Long abogadoId) {
        return disponibilidadRepository.findByAbogadoIdAndActivoTrue(abogadoId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DisponibilidadDTO guardar(DisponibilidadDTO dto) {
        if (dto.getHoraFin().isBefore(dto.getHoraInicio()) || dto.getHoraFin().equals(dto.getHoraInicio())) {
            throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
        }

        Abogado abogado = abogadoRepository.findById(dto.getAbogadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Abogado no encontrado con id: " + dto.getAbogadoId()));

        String diaUpper = dto.getDiaSemana().toUpperCase().trim();

        // Validar no solapamiento en disponibilidad del mismo abogado y día
        List<DisponibilidadAbogado> existentes = disponibilidadRepository.findByAbogadoIdAndDiaSemanaAndActivoTrue(dto.getAbogadoId(), diaUpper);
        for (DisponibilidadAbogado disp : existentes) {
            if (dto.getId() != null && dto.getId().equals(disp.getId())) continue;
            boolean overlap = dto.getHoraInicio().isBefore(disp.getHoraFin()) && dto.getHoraFin().isAfter(disp.getHoraInicio());
            if (overlap) {
                throw new BadRequestException("Existe una franja de disponibilidad que se solapa con el horario ingresado");
            }
        }

        DisponibilidadAbogado disp;
        if (dto.getId() != null) {
            disp = disponibilidadRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Disponibilidad no encontrada"));
            disp.setDiaSemana(diaUpper);
            disp.setHoraInicio(dto.getHoraInicio());
            disp.setHoraFin(dto.getHoraFin());
            disp.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        } else {
            disp = DisponibilidadAbogado.builder()
                    .abogado(abogado)
                    .diaSemana(diaUpper)
                    .horaInicio(dto.getHoraInicio())
                    .horaFin(dto.getHoraFin())
                    .activo(true)
                    .build();
        }

        DisponibilidadAbogado guardada = disponibilidadRepository.save(disp);
        return mapToDTO(guardada);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        DisponibilidadAbogado disp = disponibilidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilidad no encontrada con id: " + id));
        disp.setActivo(false);
        disponibilidadRepository.save(disp);
    }

    private DisponibilidadDTO mapToDTO(DisponibilidadAbogado d) {
        return DisponibilidadDTO.builder()
                .id(d.getId())
                .abogadoId(d.getAbogado().getId())
                .diaSemana(d.getDiaSemana())
                .horaInicio(d.getHoraInicio())
                .horaFin(d.getHoraFin())
                .activo(d.getActivo())
                .build();
    }
}
