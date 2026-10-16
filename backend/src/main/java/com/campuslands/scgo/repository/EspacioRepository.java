package com.campuslands.scgo.repository;

import com.campuslands.scgo.model.Espacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspacioRepository extends JpaRepository<Espacio, Long> {

    Optional<Espacio> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    List<Espacio> findByEstado(String estado);

    List<Espacio> findBySector(String sector);

    List<Espacio> findByTipo(String tipo);
}
