package com.campuslands.scgo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class PrestamoRequestDTO {

    @NotNull(message = "El id del recurso es obligatorio")
    private Long recursoId;

    @NotNull(message = "El id del usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "La fecha de devolución estimada es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaDevolucionEstimada;

    private String observaciones;
}
