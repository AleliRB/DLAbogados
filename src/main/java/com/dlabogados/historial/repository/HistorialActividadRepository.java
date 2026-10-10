package com.dlabogados.historial.repository;

import com.dlabogados.historial.entity.HistorialActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialActividadRepository extends JpaRepository<HistorialActividad, Long> {
    List<HistorialActividad> findByTipoEntidadAndEntidadIdOrderByFechaDesc(String tipoEntidad, Long entidadId);
    List<HistorialActividad> findByTipoEntidadOrderByFechaDesc(String tipoEntidad);
    List<HistorialActividad> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    List<HistorialActividad> findAllByOrderByFechaDesc();
}
