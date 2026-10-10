package com.dlabogados.pago.controller;

import com.dlabogados.pago.dto.PagoCreateRequest;
import com.dlabogados.pago.dto.PagoResponse;
import com.dlabogados.pago.dto.ResumenPagoCasoResponse;
import com.dlabogados.pago.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @GetMapping("/pagos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<List<PagoResponse>> obtenerTodos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long clienteId) {
        if (clienteId != null) {
            return ResponseEntity.ok(pagoService.obtenerPorCliente(clienteId));
        }
        return ResponseEntity.ok(pagoService.obtenerTodos(estado));
    }

    @GetMapping("/pagos/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<List<PagoResponse>> obtenerPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pagoService.obtenerPorCliente(clienteId));
    }

    @GetMapping("/casos/{casoId}/pagos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<List<PagoResponse>> obtenerPorCaso(@PathVariable Long casoId) {
        return ResponseEntity.ok(pagoService.obtenerPorCaso(casoId));
    }

    @GetMapping("/casos/{casoId}/saldo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<ResumenPagoCasoResponse> obtenerResumenSaldoCaso(@PathVariable Long casoId) {
        return ResponseEntity.ok(pagoService.obtenerResumenSaldoCaso(casoId));
    }

    @PostMapping("/pagos")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<PagoResponse> registrarPagoDirecto(
            @Valid @RequestBody PagoCreateRequest request) {
        PagoResponse response = pagoService.registrarPagoDirecto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/casos/{casoId}/pagos")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<PagoResponse> registrarPago(
            @PathVariable Long casoId,
            @Valid @RequestBody PagoCreateRequest request) {

        PagoResponse response = pagoService.registrarPago(casoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/pagos/crear-preferencia")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<com.dlabogados.pago.dto.MercadoPagoPreferenceResponse> crearPreferenciaPago(
            @Valid @RequestBody com.dlabogados.pago.dto.MercadoPagoPreferenceRequest request) {
        return ResponseEntity.ok(pagoService.crearPreferenciaPago(request));
    }

    @PostMapping("/pagos/confirmar-online")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<PagoResponse> confirmarPagoOnline(
            @RequestParam Long citaId,
            @RequestParam(required = false) String paymentId,
            @RequestParam(required = false, defaultValue = "approved") String status) {
        return ResponseEntity.ok(pagoService.confirmarPagoOnline(citaId, paymentId, status));
    }

    @PatchMapping("/pagos/{pagoId}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagoResponse> anularPago(@PathVariable Long pagoId) {
        return ResponseEntity.ok(pagoService.anularPago(pagoId));
    }
}
