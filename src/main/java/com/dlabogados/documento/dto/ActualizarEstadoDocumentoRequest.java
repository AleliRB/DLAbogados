package com.dlabogados.documento.dto;

import com.dlabogados.documento.entity.EstadoDocumento;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarEstadoDocumentoRequest {

    @NotNull(message = "El estado del documento es obligatorio")
    private EstadoDocumento estado;

    private String observaciones;
}
