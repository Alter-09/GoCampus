package com.campuslands.scgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaResponseDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private Long usuarioId;
    private String usuarioNombre;
    private Long espacioId;
    private String espacioNombre;
    private Long recursoId;
    private String recursoNombre;
    private String prioridad;
    private String categoria;
    private String estado;
    private Long agenteAsignadoId;
    private String agenteAsignadoNombre;
    private LocalDateTime fechaReporte;
    private LocalDateTime fechaResolucion;
    private TriajeIaResultadoDTO triajeIa;
}
