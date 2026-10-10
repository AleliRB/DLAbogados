package com.dlabogados.caso.repository;

import com.dlabogados.caso.entity.Caso;
import com.dlabogados.caso.entity.EstadoCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CasoRepository extends JpaRepository<Caso, Long> {
    Optional<Caso> findByCodigo(String codigo);
    Boolean existsByCodigo(String codigo);
    List<Caso> findByClienteId(Long clienteId);
    List<Caso> findByEstado(EstadoCaso estado);

    @Query("SELECT COUNT(c) FROM Caso c WHERE c.codigo LIKE CONCAT('CAS-', :year, '-%')")
    Long countByCodigoYear(String year);
}
