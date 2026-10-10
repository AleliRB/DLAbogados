package com.dlabogados.abogado.controller;

import com.dlabogados.abogado.dto.AbogadoRequest;
import com.dlabogados.abogado.dto.AbogadoResponse;
import com.dlabogados.abogado.service.AbogadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/abogados")
@RequiredArgsConstructor
public class AbogadoController {

    private final AbogadoService abogadoService;

    @GetMapping
    public ResponseEntity<List<AbogadoResponse>> obtenerTodos() {
        return ResponseEntity.ok(abogadoService.obtenerTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<AbogadoResponse>> obtenerActivos() {
        return ResponseEntity.ok(abogadoService.obtenerActivos());
    }

    @GetMapping("/servicio/{servicioId}")
    public ResponseEntity<List<AbogadoResponse>> obtenerPorServicio(@PathVariable Long servicioId) {
        return ResponseEntity.ok(abogadoService.obtenerPorServicio(servicioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbogadoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(abogadoService.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<AbogadoResponse> obtenerPorUsuarioId(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(abogadoService.obtenerPorUsuarioId(usuarioId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AbogadoResponse> registrar(@Valid @RequestBody AbogadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(abogadoService.registrar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<AbogadoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody AbogadoRequest request) {
        return ResponseEntity.ok(abogadoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestParam Boolean activo) {
        abogadoService.cambiarEstadoActivo(id, activo);
        return ResponseEntity.noContent().build();
    }
}
