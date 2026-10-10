package com.dlabogados.caso.dto;

import com.dlabogados.caso.entity.EstadoCaso;
import com.dlabogados.caso.entity.PrioridadCaso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CasoResponse {
    private Long id;
    private String codigo;
    private Long clienteId;
    private String clienteNombre;
    private String clienteDni;
    private Long servicioId;
    private String servicioNombre;
    private String especialidadNombre;
    private Long casoOrigenId;
    private String casoOrigenCodigo;
    private String titulo;
    private String descripcion;
    private EstadoCaso estado;
    private PrioridadCaso prioridad;
    private BigDecimal montoAcordado;
    private BigDecimal totalPagado;
    private BigDecimal saldoPendiente;
    private Double porcentajePagado;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private LocalDateTime fechaActualizacion;
    private Long abogadoResponsableId;
    private String abogadoResponsableNombre;
    private List<CasoAbogadoResponse> abogadosAsignados;
}
