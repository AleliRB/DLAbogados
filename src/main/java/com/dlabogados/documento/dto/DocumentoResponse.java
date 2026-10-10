package com.dlabogados.documento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoResponse {
    private Long id;
    private Long casoId;
    private String casoCodigo;
    private Long usuarioId;
    private String usuarioEmail;
    private String nombreOriginal;
    private String nombreArchivo;
    private String tipoMime;
    private String categoria;
    private String descripcion;
    private Boolean visibleCliente;
    private LocalDateTime fechaSubida;
    private Boolean activo;
    private com.dlabogados.documento.entity.EstadoDocumento estado;
    private String observaciones;
    private Integer version;
}
