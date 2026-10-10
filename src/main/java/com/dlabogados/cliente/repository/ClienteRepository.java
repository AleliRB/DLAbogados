package com.dlabogados.cliente.repository;

import com.dlabogados.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUsuarioId(Long usuarioId);
    Optional<Cliente> findByUsuarioEmail(String email);
    Optional<Cliente> findByDni(String dni);
    Boolean existsByDni(String dni);
}
