package com.dlabogados.disponibilidad.entity;

import com.dlabogados.abogado.entity.Abogado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "disponibilidad_abogado")
public class DisponibilidadAbogado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abogado_id", nullable = false)
    private Abogado abogado;

    @Column(name = "dia_semana", nullable = false, length = 20)
    private String diaSemana; // e.g. LUNES, MARTES, etc.

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
