package com.campuslands.scgo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas_espacios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaEspacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = false)
    private Espacio espacio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "cantidad_personas", nullable = false)
    private Integer cantidadPersonas;

    @Column(length = 20)
    @Builder.Default
    private String estado = "activa"; // 'activa', 'en_curso', 'completada', 'cancelada'

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;
}
