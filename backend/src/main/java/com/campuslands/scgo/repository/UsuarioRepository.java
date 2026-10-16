package com.campuslands.scgo.repository;

import com.campuslands.scgo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByRol(String rol);

    List<Usuario> findByRolAndEstado(String rol, String estado);

    /**
     * Busca el agente de soporte activo con menor número de tickets en curso/triaje
     * para balancear automáticamente la carga de trabajo en el triaje de incidencias.
     */
    @Query("""
        SELECT u FROM Usuario u 
        WHERE u.rol = 'soporte' AND u.estado = 'activo'
        ORDER BY (
            SELECT COUNT(i) FROM Incidencia i 
            WHERE i.agenteAsignado = u AND i.estado IN ('reportada', 'en_triaje', 'en_proceso')
        ) ASC
    """)
    List<Usuario> findAgentesSoporteOrdenadosPorCarga();
}
