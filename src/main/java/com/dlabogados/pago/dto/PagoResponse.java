package com.dlabogados.pago.dto;

import com.dlabogados.pago.entity.EstadoPago;
import com.dlabogados.pago.entity.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponse {
    private Long id;
    private Long casoId;
    private String casoCodigo;
    private Long citaId;
    private Long clienteId;
    private String clienteNombre;
    private String concepto;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private EstadoPago estadoPago;
    private String numeroOperacion;
    private String comprobanteUrl;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaRegistro;
    private String observacion;
}
