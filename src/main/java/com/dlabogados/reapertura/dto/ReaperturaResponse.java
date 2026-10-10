package com.dlabogados.reapertura.dto;

import com.dlabogados.reapertura.entity.EstadoReapertura;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReaperturaResponse {
    private Long id;
    private Long casoAnteriorId;
    private String casoAnteriorCodigo;
    private String casoAnteriorTitulo;
    private Long clienteId;
    private String clienteNombre;
    private Long abogadoPreferidoId;
    private String abogadoPreferidoNombre;
    private Long nuevoCasoId;
    private String nuevoCasoCodigo;
    private String motivo;
    private EstadoReapertura estado;
    private String observacionRespuesta;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;
}
