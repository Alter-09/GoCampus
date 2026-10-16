package com.campuslands.scgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoResponseDTO {
    private Long id;
    private Long recursoId;
    private String recursoNombre;
    private String recursoCodigoInventario;
    private String recursoCategoria;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;
    private LocalDateTime fechaPrestamo;
    private LocalDateTime fechaDevolucionEstimada;
    private LocalDateTime fechaDevolucionReal;
    private String estado;
    private String observaciones;
}
