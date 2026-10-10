package com.dlabogados.pago.service;

import com.dlabogados.pago.dto.PagoCreateRequest;
import com.dlabogados.pago.dto.PagoResponse;
import com.dlabogados.pago.dto.ResumenPagoCasoResponse;

import java.util.List;

public interface PagoService {
    List<PagoResponse> obtenerPorCaso(Long casoId);
    List<PagoResponse> obtenerTodos(String estado);
    List<PagoResponse> obtenerPorCliente(Long clienteId);
    ResumenPagoCasoResponse obtenerResumenSaldoCaso(Long casoId);
    PagoResponse registrarPago(Long casoId, PagoCreateRequest request);
    PagoResponse registrarPagoDirecto(PagoCreateRequest request);
    PagoResponse anularPago(Long pagoId);
    
    com.dlabogados.pago.dto.MercadoPagoPreferenceResponse crearPreferenciaPago(com.dlabogados.pago.dto.MercadoPagoPreferenceRequest request);
    PagoResponse confirmarPagoOnline(Long citaId, String paymentId, String status);
}
