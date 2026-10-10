package com.dlabogados.documento.repository;

import com.dlabogados.documento.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findByCasoIdAndActivoTrue(Long casoId);
    List<Documento> findByCasoIdAndVisibleClienteTrueAndActivoTrue(Long casoId);
    List<Documento> findAllByActivoTrueOrderByFechaSubidaDesc();
    List<Documento> findByEstadoAndActivoTrueOrderByFechaSubidaDesc(com.dlabogados.documento.entity.EstadoDocumento estado);
    List<Documento> findByCasoIdAndEstadoAndActivoTrueOrderByFechaSubidaDesc(Long casoId, com.dlabogados.documento.entity.EstadoDocumento estado);
    long countByEstadoAndActivoTrue(com.dlabogados.documento.entity.EstadoDocumento estado);
}
