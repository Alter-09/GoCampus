package com.campuslands.scgo.repository;

import com.campuslands.scgo.model.ReservaEspacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaEspacioRepository extends JpaRepository<ReservaEspacio, Long> {

    /**
     * Detección estricta de solapamiento de horarios (Módulo 1 Core):
     * Dos reservas se solapan si: (r.fechaInicio < :fechaFin) AND (r.fechaFin > :fechaInicio).
     * Solo se consideran reservas activas o en curso.
     */
    @Query("""
        SELECT COUNT(r) > 0 FROM ReservaEspacio r 
        WHERE r.espacio.id = :espacioId 
          AND r.estado IN ('activa', 'en_curso')
          AND r.fechaInicio < :fechaFin 
          AND r.fechaFin > :fechaInicio
          AND (:reservaId IS NULL OR r.id != :reservaId)
    """)
    boolean existeSolapamiento(
        @Param("espacioId") Long espacioId,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin,
        @Param("reservaId") Long reservaId
    );

    /**
     * Consultas para calendario semanal por espacio
     */
    @Query("""
        SELECT r FROM ReservaEspacio r 
        JOIN FETCH r.espacio e 
        JOIN FETCH r.usuario u 
        WHERE e.id = :espacioId 
          AND r.fechaFin >= :desde 
          AND r.fechaInicio <= :hasta 
          AND r.estado IN ('activa', 'en_curso')
        ORDER BY r.fechaInicio ASC
    """)
    List<ReservaEspacio> findByEspacioIdAndRango(
        @Param("espacioId") Long espacioId,
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta
    );

    /**
     * Consultas para calendario semanal por usuario (estudiante o trainer)
     */
    @Query("""
        SELECT r FROM ReservaEspacio r 
        JOIN FETCH r.espacio e 
        JOIN FETCH r.usuario u 
        WHERE u.id = :usuarioId 
          AND r.fechaFin >= :desde 
          AND r.fechaInicio <= :hasta 
          AND r.estado IN ('activa', 'en_curso')
        ORDER BY r.fechaInicio ASC
    """)
    List<ReservaEspacio> findByUsuarioIdAndRango(
        @Param("usuarioId") Long usuarioId,
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta
    );

    List<ReservaEspacio> findByEspacioIdAndEstado(Long espacioId, String estado);

    List<ReservaEspacio> findByUsuarioId(Long usuarioId);
}
