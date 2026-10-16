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
public class ReservaResponseDTO {
    private Long id;
    private Long espacioId;
    private String espacioNombre;
    private Integer espacioAforoMaximo;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer cantidadPersonas;
    private String estado;
    private String motivo;
    private LocalDateTime fechaRegistro;
}
