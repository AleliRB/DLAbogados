package com.dlabogados.cita.controller;

import com.dlabogados.cita.dto.*;
import com.dlabogados.cita.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<List<CitaResponse>> obtenerTodas(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long abogadoId,
            @RequestParam(required = false) Long casoId) {

        if (clienteId != null) {
            return ResponseEntity.ok(citaService.obtenerPorCliente(clienteId));
        }
        if (abogadoId != null) {
            return ResponseEntity.ok(citaService.obtenerPorAbogado(abogadoId));
        }
        if (casoId != null) {
            return ResponseEntity.ok(citaService.obtenerPorCaso(casoId));
        }
        return ResponseEntity.ok(citaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<CitaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerPorId(id));
    }

    @GetMapping("/ocupadas")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<List<CitaOcupadaDTO>> obtenerOcupadas(
            @RequestParam Long abogadoId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fecha) {
        return ResponseEntity.ok(citaService.obtenerOcupadas(abogadoId, fecha));
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<List<HorarioDisponibleDTO>> obtenerDisponibles(
            @RequestParam Long abogadoId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fecha,
            @RequestParam(required = false) Long servicioId) {
        return ResponseEntity.ok(citaService.obtenerHorariosDisponibles(abogadoId, fecha, servicioId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<CitaResponse> crear(@Valid @RequestBody CitaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crear(request));
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<CitaResponse> confirmar(@PathVariable Long id, @RequestBody(required = false) CitaConfirmarRequest request) {
        if (request == null) request = new CitaConfirmarRequest();
        return ResponseEntity.ok(citaService.confirmar(id, request));
    }

    @PatchMapping("/{id}/atender")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<CitaResponse> atender(@PathVariable Long id, @RequestParam(required = false) String observaciones) {
        return ResponseEntity.ok(citaService.atender(id, observaciones));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<CitaResponse> cancelar(@PathVariable Long id, @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(citaService.cancelar(id, motivo));
    }

    @PatchMapping("/{id}/reprogramar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<CitaResponse> reprogramar(@PathVariable Long id, @Valid @RequestBody CitaReprogramarRequest request) {
        return ResponseEntity.ok(citaService.reprogramar(id, request));
    }
}
