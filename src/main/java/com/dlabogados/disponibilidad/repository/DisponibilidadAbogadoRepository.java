package com.dlabogados.disponibilidad.repository;

import com.dlabogados.disponibilidad.entity.DisponibilidadAbogado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisponibilidadAbogadoRepository extends JpaRepository<DisponibilidadAbogado, Long> {
    List<DisponibilidadAbogado> findByAbogadoIdAndActivoTrue(Long abogadoId);
    List<DisponibilidadAbogado> findByAbogadoId(Long abogadoId);
    List<DisponibilidadAbogado> findByAbogadoIdAndDiaSemanaAndActivoTrue(Long abogadoId, String diaSemana);
}
