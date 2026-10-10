package com.dlabogados.seguimiento.repository;

import com.dlabogados.seguimiento.entity.SeguimientoCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeguimientoCasoRepository extends JpaRepository<SeguimientoCaso, Long> {
    List<SeguimientoCaso> findByCasoIdOrderByFechaDesc(Long casoId);
    List<SeguimientoCaso> findByCasoIdAndVisibleClienteTrueOrderByFechaDesc(Long casoId);
}
