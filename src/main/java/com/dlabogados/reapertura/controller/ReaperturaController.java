package com.dlabogados.reapertura.controller;

import com.dlabogados.reapertura.dto.ReaperturaRequest;
import com.dlabogados.reapertura.dto.ReaperturaRespuestaRequest;
import com.dlabogados.reapertura.dto.ReaperturaResponse;
import com.dlabogados.reapertura.service.ReaperturaService;
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
public class ReaperturaController {

    private final ReaperturaService reaperturaService;

    @GetMapping("/reaperturas")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<List<ReaperturaResponse>> obtenerTodas(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long abogadoId) {

        if (clienteId != null) {
            return ResponseEntity.ok(reaperturaService.obtenerPorCliente(clienteId));
        }
        if (abogadoId != null) {
            return ResponseEntity.ok(reaperturaService.obtenerPorAbogadoPreferido(abogadoId));
        }
        return ResponseEntity.ok(reaperturaService.obtenerTodas());
    }

    @GetMapping("/reaperturas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<ReaperturaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reaperturaService.obtenerPorId(id));
    }

    @PostMapping("/casos/{casoId}/reaperturas")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<ReaperturaResponse> solicitarReapertura(
            @PathVariable Long casoId,
            @Valid @RequestBody ReaperturaRequest request) {

        ReaperturaResponse response = reaperturaService.solicitarReapertura(casoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/reaperturas/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReaperturaResponse> aprobarReapertura(
            @PathVariable Long id,
            @RequestBody(required = false) ReaperturaRespuestaRequest request) {

        return ResponseEntity.ok(reaperturaService.aprobarReapertura(id, request));
    }

    @PatchMapping("/reaperturas/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReaperturaResponse> rechazarReapertura(
            @PathVariable Long id,
            @RequestBody(required = false) ReaperturaRespuestaRequest request) {

        return ResponseEntity.ok(reaperturaService.rechazarReapertura(id, request));
    }
}
