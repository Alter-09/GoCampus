package com.campuslands.scgo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidencias")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id")
    private Espacio espacio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id")
    private RecursoTecnologico recurso;

    @Column(length = 20)
    @Builder.Default
    private String prioridad = "media"; // 'baja', 'media', 'alta', 'critica'

    @Column(length = 50)
    private String categoria; // 'infraestructura', 'tecnologia', 'logistica', 'bienestar', 'otro'

    @Column(length = 20)
    @Builder.Default
    private String estado = "reportada"; // 'reportada', 'en_triaje', 'en_proceso', 'resuelta', 'cerrada'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_asignado_id")
    private Usuario agenteAsignado;

    @CreationTimestamp
    @Column(name = "fecha_reporte", updatable = false)
    private LocalDateTime fechaReporte;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;
}
