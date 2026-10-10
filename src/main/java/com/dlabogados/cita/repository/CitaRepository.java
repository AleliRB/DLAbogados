package com.dlabogados.cita.repository;

import com.dlabogados.cita.entity.Cita;
import com.dlabogados.cita.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByClienteId(Long clienteId);
    List<Cita> findByAbogadoId(Long abogadoId);
    List<Cita> findByCasoId(Long casoId);

    @Query("SELECT c FROM Cita c WHERE c.abogado.id = :abogadoId AND c.fecha = :fecha AND c.estado IN (com.dlabogados.cita.entity.EstadoCita.PENDIENTE_PAGO, com.dlabogados.cita.entity.EstadoCita.PENDIENTE_CONFIRMACION, com.dlabogados.cita.entity.EstadoCita.PENDIENTE, com.dlabogados.cita.entity.EstadoCita.CONFIRMADA, com.dlabogados.cita.entity.EstadoCita.REPROGRAMADA, com.dlabogados.cita.entity.EstadoCita.EN_ATENCION) AND ((c.horaInicio < :horaFin AND c.horaFin > :horaInicio))")
    List<Cita> findOverlappingCitas(Long abogadoId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin);

    @Query("SELECT c FROM Cita c WHERE c.abogado.id = :abogadoId AND c.fecha = :fecha AND c.estado IN (com.dlabogados.cita.entity.EstadoCita.PENDIENTE_PAGO, com.dlabogados.cita.entity.EstadoCita.PENDIENTE_CONFIRMACION, com.dlabogados.cita.entity.EstadoCita.PENDIENTE, com.dlabogados.cita.entity.EstadoCita.CONFIRMADA, com.dlabogados.cita.entity.EstadoCita.REPROGRAMADA, com.dlabogados.cita.entity.EstadoCita.EN_ATENCION)")
    List<Cita> findCitasActivasDelDia(Long abogadoId, LocalDate fecha);

    @Query("SELECT c FROM Cita c WHERE c.abogado.id = :abogadoId AND c.fecha = :fecha AND c.estado IN (com.dlabogados.cita.entity.EstadoCita.PENDIENTE_PAGO, com.dlabogados.cita.entity.EstadoCita.PENDIENTE_CONFIRMACION, com.dlabogados.cita.entity.EstadoCita.PENDIENTE, com.dlabogados.cita.entity.EstadoCita.CONFIRMADA, com.dlabogados.cita.entity.EstadoCita.REPROGRAMADA, com.dlabogados.cita.entity.EstadoCita.EN_ATENCION) AND c.id <> :excludeCitaId AND ((c.horaInicio < :horaFin AND c.horaFin > :horaInicio))")
    List<Cita> findOverlappingCitasExcluding(Long abogadoId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Long excludeCitaId);
}
