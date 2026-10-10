package com.dlabogados.abogado.repository;

import com.dlabogados.abogado.entity.Abogado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AbogadoRepository extends JpaRepository<Abogado, Long> {
    Optional<Abogado> findByUsuarioId(Long usuarioId);
    Optional<Abogado> findByDni(String dni);
    Optional<Abogado> findByNumeroColegiatura(String numeroColegiatura);
    Boolean existsByDni(String dni);
    Boolean existsByNumeroColegiatura(String numeroColegiatura);
    List<Abogado> findByActivoTrue();
    List<Abogado> findDistinctByEspecialidadesIdAndActivoTrue(Long especialidadId);
}
