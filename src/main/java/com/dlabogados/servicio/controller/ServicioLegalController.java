package com.dlabogados.servicio.controller;

import com.dlabogados.servicio.dto.ServicioLegalDTO;
import com.dlabogados.servicio.service.ServicioLegalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioLegalController {

    private final ServicioLegalService servicioLegalService;

    @GetMapping
    public ResponseEntity<List<ServicioLegalDTO>> obtenerTodos() {
        return ResponseEntity.ok(servicioLegalService.obtenerTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ServicioLegalDTO>> obtenerActivos() {
        return ResponseEntity.ok(servicioLegalService.obtenerActivos());
    }

    @GetMapping("/especialidad/{especialidadId}")
    public ResponseEntity<List<ServicioLegalDTO>> obtenerPorEspecialidad(@PathVariable Long especialidadId) {
        return ResponseEntity.ok(servicioLegalService.obtenerPorEspecialidad(especialidadId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioLegalDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicioLegalService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioLegalDTO> crear(@Valid @RequestBody ServicioLegalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioLegalService.crear(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioLegalDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ServicioLegalDTO dto) {
        return ResponseEntity.ok(servicioLegalService.actualizar(id, dto));
    }
}
