package com.campuslands.scgo.repository;

import com.campuslands.scgo.model.RecursoTecnologico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecursoTecnologicoRepository extends JpaRepository<RecursoTecnologico, Long> {

    Optional<RecursoTecnologico> findByCodigoInventario(String codigoInventario);

    boolean existsByCodigoInventario(String codigoInventario);

    List<RecursoTecnologico> findByCategoria(String categoria);

    List<RecursoTecnologico> findByEstado(String estado);

    List<RecursoTecnologico> findByCategoriaAndEstado(String categoria, String estado);
}
