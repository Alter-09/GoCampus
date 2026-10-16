package com.campuslands.scgo.dto;

import jakarta.validation.constraints.Email;
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
public class UsuarioDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    private String documentoIdentidad;
    private String telefono;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "estudiante|trainer|administrador|soporte", message = "El rol debe ser estudiante, trainer, administrador o soporte")
    private String rol;

    @Pattern(regexp = "activo|inactivo|suspendido", message = "El estado debe ser activo, inactivo o suspendido")
    private String estado;

    private LocalDateTime fechaCreacion;
}
