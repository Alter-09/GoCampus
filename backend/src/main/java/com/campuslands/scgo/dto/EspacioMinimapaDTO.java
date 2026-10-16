package com.campuslands.scgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspacioMinimapaDTO {
    private Long id;
    private String nombre;
    private String tipo;
    private Integer aforoMaximo;
    private String sector;
    private String piso;
    private BigDecimal coordenadaX;
    private BigDecimal coordenadaY;
    private String ubicacion;
    private boolean ocupado;
    private Long reservaActualId;
    private String eventoTitulo;
    private String estadoEspacio; // 'disponible', 'mantenimiento', 'inhabilitado'
}
