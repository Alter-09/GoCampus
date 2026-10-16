package com.campuslands.scgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecursoDTO {
    private Long id;

    @NotBlank(message = "El código de inventario es obligatorio")
    private String codigoInventario;

    @NotBlank(message = "El nombre del recurso es obligatorio")
    private String nombre;

    @NotBlank(message = "La categoría es obligatoria")
    @Pattern(regexp = "mouse|teclado|cable_red|cable_hdmi|adaptador|otros", message = "Categoría de periférico inválida")
    private String categoria;

    @Pattern(regexp = "operativo|en_prestamo|mantenimiento|inhabilitado", message = "Estado inválido")
    private String estado;

    private Long espacioAsignadoId;
    private String espacioAsignadoNombre;
    private LocalDateTime fechaIngreso;
}
