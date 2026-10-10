package com.dlabogados.reapertura.repository;

import com.dlabogados.reapertura.entity.EstadoReapertura;
import com.dlabogados.reapertura.entity.Reapertura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReaperturaRepository extends JpaRepository<Reapertura, Long> {
    List<Reapertura> findByClienteId(Long clienteId);
    List<Reapertura> findByAbogadoPreferidoId(Long abogadoId);
    List<Reapertura> findByCasoAnteriorId(Long casoAnteriorId);
    List<Reapertura> findByEstado(EstadoReapertura estado);
}
