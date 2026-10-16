package com.campuslands.scgo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "espacios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(nullable = false, length = 30)
    private String tipo; // 'aula', 'cubiculo', 'auditorio', 'sala_juntas', 'sala_multiuso'

    @Column(name = "aforo_maximo", nullable = false)
    private Integer aforoMaximo;

    @Column(length = 20)
    @Builder.Default
    private String estado = "disponible"; // 'disponible', 'mantenimiento', 'inhabilitado'

    @Column(length = 50)
    @Builder.Default
    private String sector = "Cabo Canaveral"; // 'Cabo Canaveral', 'ISS', 'Principal', etc.

    @Column(length = 20)
    @Builder.Default
    private String piso = "Piso 1";

    @Column(name = "coordenada_x", precision = 6, scale = 2)
    private BigDecimal coordenadaX;

    @Column(name = "coordenada_y", precision = 6, scale = 2)
    private BigDecimal coordenadaY;

    @Column(length = 100)
    private String ubicacion;
}
