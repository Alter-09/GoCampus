package com.campuslands.scgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaRequestDTO {

    @NotBlank(message = "El título de la incidencia es obligatorio")
    private String titulo;

    @NotBlank(message = "La descripción de la novedad es obligatoria")
    private String descripcion;

    @NotNull(message = "El id del usuario reportante es obligatorio")
    private Long usuarioId;

    private Long espacioId;
    private Long recursoId;
    private String categoriaManual;
    private String prioridadManual;
}
