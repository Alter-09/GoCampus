package com.campuslands.scgo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "recursos_tecnologicos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecursoTecnologico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_inventario", nullable = false, unique = true, length = 50)
    private String codigoInventario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String categoria; // 'mouse', 'teclado', 'cable_red', 'cable_hdmi', 'adaptador', 'otros'

    @Column(length = 20)
    @Builder.Default
    private String estado = "operativo"; // 'operativo', 'en_prestamo', 'mantenimiento', 'inhabilitado'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_asignado_id")
    private Espacio espacioAsignado;

    @CreationTimestamp
    @Column(name = "fecha_ingreso", updatable = false)
    private LocalDateTime fechaIngreso;
}
