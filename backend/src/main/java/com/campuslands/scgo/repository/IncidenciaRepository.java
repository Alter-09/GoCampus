package com.campuslands.scgo.repository;

import com.campuslands.scgo.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findByEstado(String estado);

    List<Incidencia> findByPrioridad(String prioridad);

    List<Incidencia> findByEspacioId(Long espacioId);

    List<Incidencia> findByRecursoId(Long recursoId);

    List<Incidencia> findByUsuarioId(Long usuarioId);

    List<Incidencia> findByAgenteAsignadoId(Long agenteId);
}
