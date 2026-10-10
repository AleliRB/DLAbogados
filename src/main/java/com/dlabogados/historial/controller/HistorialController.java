package com.dlabogados.historial.controller;

import com.dlabogados.historial.dto.HistorialActividadResponse;
import com.dlabogados.historial.service.HistorialActividadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historial")
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialActividadService historialService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<List<HistorialActividadResponse>> obtenerTodos(
            @RequestParam(required = false) String tipoEntidad,
            @RequestParam(required = false) Long entidadId) {

        if (tipoEntidad != null && entidadId != null) {
            return ResponseEntity.ok(historialService.obtenerPorEntidad(tipoEntidad, entidadId));
        } else if (tipoEntidad != null) {
            return ResponseEntity.ok(historialService.obtenerPorTipo(tipoEntidad));
        }
        return ResponseEntity.ok(historialService.obtenerTodos());
    }

    @GetMapping("/entidad/{tipoEntidad}/{entidadId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<List<HistorialActividadResponse>> obtenerPorEntidad(
            @PathVariable String tipoEntidad,
            @PathVariable Long entidadId) {
        return ResponseEntity.ok(historialService.obtenerPorEntidad(tipoEntidad, entidadId));
    }

    @GetMapping("/tipo/{tipoEntidad}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<List<HistorialActividadResponse>> obtenerPorTipo(
            @PathVariable String tipoEntidad) {
        return ResponseEntity.ok(historialService.obtenerPorTipo(tipoEntidad));
    }
}
