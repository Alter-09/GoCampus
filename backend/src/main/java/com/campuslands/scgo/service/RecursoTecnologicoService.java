package com.campuslands.scgo.service;

import com.campuslands.scgo.dto.RecursoDTO;

import java.util.List;

public interface RecursoTecnologicoService {
    List<RecursoDTO> listarTodos();
    RecursoDTO obtenerPorId(Long id);
    RecursoDTO crear(RecursoDTO dto);
    RecursoDTO actualizar(Long id, RecursoDTO dto);
    void cambiarEstado(Long id, String nuevoEstado);
    List<RecursoDTO> listarPorCategoria(String categoria);
    List<RecursoDTO> listarDisponiblesParaPrestamo();
}
