package com.campuslands.scgo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class ReservaRequestDTO {

    @NotNull(message = "El id del espacio es obligatorio")
    private Long espacioId;

    @NotNull(message = "El id del usuario es obligatorio")
    private Long usuarioId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicio;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaFin;

    // Campos de fecha de reserva y hora de reserva requeridos por el frontend
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaReserva;

    @JsonFormat(pattern = "HH:mm[:ss]")
    private LocalTime horaInicio;

    @JsonFormat(pattern = "HH:mm[:ss]")
    private LocalTime horaFin;

    @JsonFormat(pattern = "HH:mm[:ss]")
    private LocalTime horaReserva;

    @NotNull(message = "La cantidad de personas es obligatoria")
    @Min(value = 1, message = "Debe haber al menos 1 persona en la reserva")
    private Integer cantidadPersonas;

    private String motivo;
}
