package com.dlabogados.pago.repository;

import com.dlabogados.pago.entity.EstadoPago;
import com.dlabogados.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByCasoId(Long casoId);
    List<Pago> findByCasoIdAndEstadoPago(Long casoId, EstadoPago estadoPago);
    List<Pago> findByClienteIdOrderByFechaRegistroDesc(Long clienteId);
    List<Pago> findAllByOrderByFechaRegistroDesc();
    List<Pago> findByCitaId(Long citaId);
    List<Pago> findByEstadoPago(EstadoPago estadoPago);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.caso.id = :casoId AND p.estadoPago = 'PAGADO'")
    BigDecimal sumPagosPagadosByCasoId(Long casoId);
}
