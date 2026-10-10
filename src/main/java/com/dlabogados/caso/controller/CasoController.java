package com.dlabogados.caso.controller;

import com.dlabogados.caso.dto.*;
import com.dlabogados.caso.entity.EstadoCaso;
import com.dlabogados.caso.service.CasoService;
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
public class CasoController {

    private final CasoService casoService;

    @GetMapping("/casos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<List<CasoResponse>> obtenerTodos(
            @RequestParam(required = false) EstadoCaso estado,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long abogadoId) {

        if (clienteId != null) {
            return ResponseEntity.ok(casoService.obtenerPorCliente(clienteId));
        }
        if (abogadoId != null) {
            return ResponseEntity.ok(casoService.obtenerPorAbogado(abogadoId));
        }
        if (estado != null) {
            return ResponseEntity.ok(casoService.obtenerPorEstado(estado));
        }
        return ResponseEntity.ok(casoService.obtenerTodos());
    }

    @GetMapping("/casos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<CasoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(casoService.obtenerPorId(id));
    }

    @GetMapping("/clientes/{clienteId}/casos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<List<CasoResponse>> obtenerPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(casoService.obtenerPorCliente(clienteId));
    }

    @GetMapping("/abogados/{abogadoId}/casos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<List<CasoResponse>> obtenerPorAbogado(@PathVariable Long abogadoId) {
        return ResponseEntity.ok(casoService.obtenerPorAbogado(abogadoId));
    }

    @PostMapping("/casos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CasoResponse> crear(@Valid @RequestBody CasoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(casoService.crear(request));
    }

    @PutMapping("/casos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CasoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody CasoUpdateRequest request) {
        return ResponseEntity.ok(casoService.actualizar(id, request));
    }

    @PatchMapping("/casos/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<CasoResponse> cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoCasoRequest request) {
        return ResponseEntity.ok(casoService.cambiarEstado(id, request));
    }

    @PostMapping("/casos/{casoId}/abogados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CasoResponse> asignarAbogado(@PathVariable Long casoId, @Valid @RequestBody AsignarAbogadoRequest request) {
        return ResponseEntity.ok(casoService.asignarAbogado(casoId, request));
    }

    @DeleteMapping("/casos/{casoId}/abogados/{abogadoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CasoResponse> desasignarAbogado(@PathVariable Long casoId, @PathVariable Long abogadoId) {
        return ResponseEntity.ok(casoService.desasignarAbogado(casoId, abogadoId));
    }
}
