package com.dlabogados.disponibilidad.controller;

import com.dlabogados.disponibilidad.dto.DisponibilidadDTO;
import com.dlabogados.disponibilidad.service.DisponibilidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disponibilidad")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    @GetMapping("/abogado/{abogadoId}")
    public ResponseEntity<List<DisponibilidadDTO>> obtenerPorAbogado(@PathVariable Long abogadoId) {
        return ResponseEntity.ok(disponibilidadService.obtenerPorAbogado(abogadoId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<DisponibilidadDTO> guardar(@Valid @RequestBody DisponibilidadDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(disponibilidadService.guardar(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        disponibilidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
