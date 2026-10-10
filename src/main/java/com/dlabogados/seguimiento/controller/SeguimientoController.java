package com.dlabogados.seguimiento.controller;

import com.dlabogados.seguimiento.dto.SeguimientoRequest;
import com.dlabogados.seguimiento.dto.SeguimientoResponse;
import com.dlabogados.seguimiento.service.SeguimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/casos/{casoId}/seguimientos")
@RequiredArgsConstructor
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<List<SeguimientoResponse>> obtenerPorCaso(@PathVariable Long casoId, Authentication authentication) {
        boolean esCliente = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
        return ResponseEntity.ok(seguimientoService.obtenerPorCaso(casoId, esCliente));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<SeguimientoResponse> registrarSeguimiento(
            @PathVariable Long casoId,
            @Valid @RequestBody SeguimientoRequest request,
            Authentication authentication) {

        SeguimientoResponse response = seguimientoService.registrarSeguimiento(casoId, authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
