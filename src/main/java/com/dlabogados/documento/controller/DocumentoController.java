package com.dlabogados.documento.controller;

import com.dlabogados.documento.dto.DocumentoResponse;
import com.dlabogados.documento.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @GetMapping("/casos/{casoId}/documentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<List<DocumentoResponse>> obtenerPorCaso(@PathVariable Long casoId, Authentication authentication) {
        boolean esCliente = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
        return ResponseEntity.ok(documentoService.obtenerPorCaso(casoId, esCliente, authentication.getName()));
    }

    @PostMapping("/casos/{casoId}/documentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<DocumentoResponse> subirDocumento(
            @PathVariable Long casoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "visibleCliente", required = false, defaultValue = "true") Boolean visibleCliente,
            Authentication authentication) {

        DocumentoResponse response = documentoService.subirDocumento(
                casoId, authentication.getName(), file, categoria, descripcion, visibleCliente
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/documentos/{id}/descargar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<Resource> descargarDocumento(@PathVariable Long id, Authentication authentication) {
        boolean esCliente = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

        DocumentoResponse doc = documentoService.obtenerPorId(id);
        Resource resource = documentoService.descargarDocumento(id, esCliente, authentication.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getTipoMime() != null ? doc.getTipoMime() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getNombreOriginal() + "\"")
                .body(resource);
    }

    @GetMapping("/documentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<List<DocumentoResponse>> obtenerTodos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long casoId) {
        return ResponseEntity.ok(documentoService.obtenerTodos(estado, casoId));
    }

    @PatchMapping("/documentos/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<DocumentoResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestBody com.dlabogados.documento.dto.ActualizarEstadoDocumentoRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(documentoService.cambiarEstado(
                id, request.getEstado(), request.getObservaciones(), authentication.getName()
        ));
    }

    @PostMapping("/documentos/{id}/sustituir")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO', 'CLIENTE')")
    public ResponseEntity<DocumentoResponse> sustituirDocumento(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        return ResponseEntity.ok(documentoService.sustituirDocumento(id, file, authentication.getName()));
    }

    @DeleteMapping("/documentos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ABOGADO')")
    public ResponseEntity<Void> eliminarDocumento(@PathVariable Long id) {
        documentoService.eliminarDocumento(id);
        return ResponseEntity.noContent().build();
    }
}
