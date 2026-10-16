package com.campuslands.scgo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspacioDTO {
    private Long id;

    @NotBlank(message = "El nombre del espacio es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo de espacio es obligatorio")
    @Pattern(regexp = "aula|cubiculo|auditorio|sala_juntas|sala_multiuso", message = "Tipo de espacio inválido")
    private String tipo;

    @NotNull(message = "El aforo máximo es obligatorio")
    @Min(value = 1, message = "El aforo máximo debe ser mayor a 0")
    private Integer aforoMaximo;

    @Pattern(regexp = "disponible|mantenimiento|inhabilitado", message = "Estado de espacio inválido")
    private String estado;

    private String sector;
    private String piso;
    private BigDecimal coordenadaX;
    private BigDecimal coordenadaY;
    private String ubicacion;
}
