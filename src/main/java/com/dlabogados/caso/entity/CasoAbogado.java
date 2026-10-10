package com.dlabogados.caso.entity;

import com.dlabogados.abogado.entity.Abogado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "caso_abogado")
public class CasoAbogado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "caso_id", nullable = false)
    private Caso caso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "abogado_id", nullable = false)
    private Abogado abogado;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Builder.Default
    @Column(name = "es_responsable", nullable = false)
    private Boolean esResponsable = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(length = 500)
    private String observacion;

    @PrePersist
    protected void onCreate() {
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDateTime.now();
        }
    }
}
