package com.campuslands.scgo.repository;

import com.campuslands.scgo.model.PrestamoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrestamoRecursoRepository extends JpaRepository<PrestamoRecurso, Long> {

    /**
     * Cuenta la cantidad de préstamos activos que tiene un usuario (máximo 2 permitidos).
     */
    long countByUsuarioIdAndEstado(Long usuarioId, String estado);

    /**
     * Verifica si el usuario tiene préstamos con devolución vencida o en mora.
     */
    @Query("""
        SELECT COUNT(p) > 0 FROM PrestamoRecurso p 
        WHERE p.usuario.id = :usuarioId 
          AND (
              p.estado = 'atrasado' 
              OR (p.estado = 'activo' AND p.fechaDevolucionEstimada < :ahora)
          )
    """)
    boolean tienePrestamosMorosos(
        @Param("usuarioId") Long usuarioId,
        @Param("ahora") LocalDateTime ahora
    );

    /**
     * Busca si un periférico ya está actualmente prestado (estado activo).
     */
    Optional<PrestamoRecurso> findByRecursoIdAndEstado(Long recursoId, String estado);

    List<PrestamoRecurso> findByUsuarioId(Long usuarioId);

    List<PrestamoRecurso> findByEstado(String estado);
}
