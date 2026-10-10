package com.dlabogados.servicio.repository;

import com.dlabogados.servicio.entity.ServicioLegal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioLegalRepository extends JpaRepository<ServicioLegal, Long> {
    java.util.Optional<ServicioLegal> findByNombre(String nombre);
    List<ServicioLegal> findByActivoTrue();
    List<ServicioLegal> findByEspecialidadIdAndActivoTrue(Long especialidadId);
}

