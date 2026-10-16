package com.campuslands.scgo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "documento_identidad", unique = true, length = 20)
    private String documentoIdentidad;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, length = 20)
    private String rol; // 'estudiante', 'trainer', 'administrador', 'soporte'

    @Column(length = 20)
    @Builder.Default
    private String estado = "activo"; // 'activo', 'inactivo', 'suspendido'

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
