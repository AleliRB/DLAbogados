package com.dlabogados.reapertura.entity;

import com.dlabogados.abogado.entity.Abogado;
import com.dlabogados.caso.entity.Caso;
import com.dlabogados.cliente.entity.Cliente;
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
@Table(name = "reaperturas")
public class Reapertura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "caso_anterior_id", nullable = false)
    private Caso casoAnterior;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "abogado_preferido_id")
    private Abogado abogadoPreferido;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nuevo_caso_id", unique = true)
    private Caso nuevoCaso;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoReapertura estado;

    @Column(name = "observacion_respuesta", columnDefinition = "TEXT")
    private String observacionRespuesta;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @PrePersist
    protected void onCreate() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }
    }
}
