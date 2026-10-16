package com.campuslands.scgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriajeIaResultadoDTO {
    private String categoria;
    private String prioridad;
    private BigDecimal nivelConfianza;
    private String justificacionTecnica;
    private String accionInmediataSugerida;
    private String modeloIa;
    private Integer latenciaMs;
    private String respuestaRaw;
}
