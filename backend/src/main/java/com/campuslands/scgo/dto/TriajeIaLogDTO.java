package com.campuslands.scgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriajeIaLogDTO {
    private Long id;
    private Long incidenciaId;
    private String incidenciaTitulo;
    private String promptEntrada;
    private String categoriaSugerida;
    private String prioridadSugerida;
    private Long agenteSugeridoId;
    private String agenteSugeridoNombre;
    private BigDecimal nivelConfianza;
    private String respuestaModelo;
    private String modeloIa;
    private Integer latenciaMs;
    private Boolean fueModificadoPorHumano;
    private LocalDateTime fechaEvaluacion;
}
