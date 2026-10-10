package com.dlabogados.especialidad.controller;

import com.dlabogados.especialidad.dto.EspecialidadDTO;
import com.dlabogados.especialidad.service.EspecialidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    @GetMapping
    public ResponseEntity<List<EspecialidadDTO>> obtenerTodas() {
        return ResponseEntity.ok(especialidadService.obtenerTodas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<EspecialidadDTO>> obtenerActivas() {
        return ResponseEntity.ok(especialidadService.obtenerActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(especialidadService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EspecialidadDTO> crear(@Valid @RequestBody EspecialidadDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(especialidadService.crear(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EspecialidadDTO> actualizar(@PathVariable Long id, @Valid @RequestBody EspecialidadDTO dto) {
        return ResponseEntity.ok(especialidadService.actualizar(id, dto));
    }
}
