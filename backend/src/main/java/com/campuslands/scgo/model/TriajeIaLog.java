package com.campuslands.scgo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "triaje_ia_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriajeIaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incidencia_id", nullable = false)
    private Incidencia incidencia;

    @Column(name = "prompt_entrada", nullable = false, columnDefinition = "TEXT")
    private String promptEntrada;

    @Column(name = "categoria_sugerida", length = 50)
    private String categoriaSugerida;

    @Column(name = "prioridad_sugerida", length = 20)
    private String prioridadSugerida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_sugerido_id")
    private Usuario agenteSugerido;

    @Column(name = "nivel_confianza", precision = 3, scale = 2)
    private BigDecimal nivelConfianza;

    @Column(name = "respuesta_modelo", nullable = false, columnDefinition = "TEXT")
    private String respuestaModelo;

    @Column(name = "modelo_ia", length = 50)
    @Builder.Default
    private String modeloIa = "gemini-1.5-flash";

    @Column(name = "latencia_ms")
    private Integer latenciaMs;

    @Column(name = "fue_modificado_por_humano")
    @Builder.Default
    private Boolean fueModificadoPorHumano = false;

    @CreationTimestamp
    @Column(name = "fecha_evaluacion", updatable = false)
    private LocalDateTime fechaEvaluacion;
}
