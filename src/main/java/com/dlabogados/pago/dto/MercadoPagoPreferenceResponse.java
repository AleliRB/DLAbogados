package com.dlabogados.pago.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPreferenceResponse {
    private String preferenceId;
    private String initPoint;
    private String sandboxInitPoint;
    private String publicKey;
    private String status;
    private String externalReference;
}
