package com.campuslands.scgo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolucionRequestDTO {

    @NotBlank(message = "El estado de entrega es obligatorio")
    @Pattern(regexp = "optimo|danado", message = "El estado de entrega debe ser 'optimo' o 'danado'")
    private String estadoEntrega;

    private String observaciones;
}
