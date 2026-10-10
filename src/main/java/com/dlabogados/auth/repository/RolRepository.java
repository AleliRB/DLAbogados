package com.dlabogados.auth.repository;

import com.dlabogados.auth.entity.Rol;
import com.dlabogados.auth.entity.RolNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(RolNombre nombre);
}
