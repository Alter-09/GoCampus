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
public class CalendarioEventoDTO {
    private Long id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long espacioId;
    private String espacioNombre;
    private String sector;
    private Long usuarioId;
    private String usuarioNombre;
    private String estado;
    private Integer cantidadPersonas;
}
