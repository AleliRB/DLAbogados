package com.dlabogados.caso.repository;

import com.dlabogados.caso.entity.CasoAbogado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CasoAbogadoRepository extends JpaRepository<CasoAbogado, Long> {
    List<CasoAbogado> findByCasoId(Long casoId);
    List<CasoAbogado> findByCasoIdAndActivoTrue(Long casoId);
    List<CasoAbogado> findByAbogadoIdAndActivoTrue(Long abogadoId);
    Optional<CasoAbogado> findByCasoIdAndEsResponsableTrueAndActivoTrue(Long casoId);
    Optional<CasoAbogado> findByCasoIdAndAbogadoIdAndActivoTrue(Long casoId, Long abogadoId);

    @Query("SELECT DISTINCT ca.caso.id FROM CasoAbogado ca WHERE ca.abogado.id = :abogadoId")
    List<Long> findCasoIdsByAbogadoId(Long abogadoId);
}
