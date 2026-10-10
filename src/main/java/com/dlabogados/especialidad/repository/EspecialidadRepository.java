package com.dlabogados.especialidad.repository;

import com.dlabogados.especialidad.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
    Optional<Especialidad> findByNombre(String nombre);
    Boolean existsByNombre(String nombre);
    List<Especialidad> findByActivoTrue();
}
