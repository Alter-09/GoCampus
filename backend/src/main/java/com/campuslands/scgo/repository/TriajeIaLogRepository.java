package com.campuslands.scgo.repository;

import com.campuslands.scgo.model.TriajeIaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriajeIaLogRepository extends JpaRepository<TriajeIaLog, Long> {

    List<TriajeIaLog> findByIncidenciaId(Long incidenciaId);

    List<TriajeIaLog> findTop20ByOrderByFechaEvaluacionDesc();
}
