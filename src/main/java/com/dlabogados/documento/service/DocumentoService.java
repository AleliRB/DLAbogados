package com.dlabogados.documento.service;

import com.dlabogados.documento.dto.DocumentoResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentoService {
    List<DocumentoResponse> obtenerPorCaso(Long casoId, boolean esCliente, String userEmail);
    List<DocumentoResponse> obtenerTodos(String estado, Long casoId);
    DocumentoResponse obtenerPorId(Long id);
    DocumentoResponse subirDocumento(Long casoId, String userEmail, MultipartFile file, String categoria, String descripcion, Boolean visibleCliente);
    DocumentoResponse cambiarEstado(Long id, com.dlabogados.documento.entity.EstadoDocumento nuevoEstado, String observaciones, String userEmail);
    DocumentoResponse sustituirDocumento(Long id, MultipartFile file, String userEmail);
    Resource descargarDocumento(Long id, boolean esCliente, String userEmail);
    void eliminarDocumento(Long id);
}
