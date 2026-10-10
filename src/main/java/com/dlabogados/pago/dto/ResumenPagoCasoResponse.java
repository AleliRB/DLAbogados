package com.dlabogados.pago.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenPagoCasoResponse {
    private Long casoId;
    private String casoCodigo;
    private BigDecimal montoAcordado;
    private BigDecimal totalPagado;
    private BigDecimal saldoPendiente;
    private Double porcentajePagado;
    private List<PagoResponse> historialPagos;
}
