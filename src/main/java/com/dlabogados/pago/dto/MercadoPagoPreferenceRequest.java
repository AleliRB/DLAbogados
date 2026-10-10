package com.dlabogados.pago.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPreferenceRequest {
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "1.00", message = "El monto minimo a pagar es S/ 1.00")
    private BigDecimal monto;

    @NotBlank(message = "El concepto es obligatorio")
    private String titulo;

    private Long citaId;
    private Long casoId;
    private String emailCliente;
    private String nombreCliente;
}
