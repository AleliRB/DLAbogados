package com.dlabogados.pago.dto;

import com.dlabogados.pago.entity.EstadoPago;
import com.dlabogados.pago.entity.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagoCreateRequest {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser estrictamente mayor a 0")
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @NotNull(message = "El estado inicial del pago es obligatorio")
    private EstadoPago estadoPago = EstadoPago.PAGADO;

    private Long casoId;
    private Long citaId;
    private String concepto;
    private String numeroOperacion;
    private String comprobanteUrl;
    private String observacion;
}
