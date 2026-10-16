package com.campuslands.scgo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "prestamos_recursos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id", nullable = false)
    private RecursoTecnologico recurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "fecha_prestamo", updatable = false)
    private LocalDateTime fechaPrestamo;

    @Column(name = "fecha_devolucion_estimada", nullable = false)
    private LocalDateTime fechaDevolucionEstimada;

    @Column(name = "fecha_devolucion_real")
    private LocalDateTime fechaDevolucionReal;

    @Column(length = 20)
    @Builder.Default
    private String estado = "activo"; // 'activo', 'devuelto', 'atrasado', 'dañado'

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
