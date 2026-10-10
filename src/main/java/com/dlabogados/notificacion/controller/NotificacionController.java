package com.dlabogados.notificacion.controller;

import com.dlabogados.notificacion.dto.NotificacionResponse;
import com.dlabogados.notificacion.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificacionResponse>> obtenerPorUsuario(
            @RequestParam(required = false, defaultValue = "false") boolean noLeidas,
            Authentication authentication) {

        if (noLeidas) {
            return ResponseEntity.ok(notificacionService.obtenerNoLeidasPorUsuario(authentication.getName()));
        }
        return ResponseEntity.ok(notificacionService.obtenerPorUsuario(authentication.getName()));
    }

    @PatchMapping("/{id}/leer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/marcar-todas-leidas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marcarTodasComoLeidas(Authentication authentication) {
        notificacionService.marcarTodasComoLeidas(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
