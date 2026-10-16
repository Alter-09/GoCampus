package com.campuslands.scgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    private LocalDate fechaReserva;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private LocalTime horaReserva;
    private Integer cantidadPersonas;
    private String estado;
    private String motivo;
    private LocalDateTime fechaRegistro;
}
