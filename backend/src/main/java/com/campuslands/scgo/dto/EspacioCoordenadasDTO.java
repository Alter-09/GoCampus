package com.campuslands.scgo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspacioCoordenadasDTO {

    private String nombre;
    private String sector;
    private String piso;

    @NotNull(message = "La coordenada X es obligatoria")
    private BigDecimal coordenadaX;

    @NotNull(message = "La coordenada Y es obligatoria")
    private BigDecimal coordenadaY;
}
