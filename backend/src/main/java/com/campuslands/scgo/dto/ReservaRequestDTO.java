package com.campuslands.scgo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaRequestDTO {

    @NotNull(message = "El id del espacio es obligatorio")
    private Long espacioId;

    @NotNull(message = "El id del usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha y hora de fin es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaFin;

    @NotNull(message = "La cantidad de personas es obligatoria")
    @Min(value = 1, message = "Debe haber al menos 1 persona en la reserva")
    private Integer cantidadPersonas;

    private String motivo;
}
