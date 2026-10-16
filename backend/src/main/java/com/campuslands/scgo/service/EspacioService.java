package com.campuslands.scgo.service;

import com.campuslands.scgo.dto.EspacioCoordenadasDTO;
import com.campuslands.scgo.dto.EspacioDTO;

import java.util.List;

public interface EspacioService {
    List<EspacioDTO> listarTodos();
    EspacioDTO obtenerPorId(Long id);
    EspacioDTO crear(EspacioDTO dto);
    EspacioDTO actualizar(Long id, EspacioDTO dto);
    EspacioDTO actualizarCoordenadas(Long id, EspacioCoordenadasDTO dto);
    void cambiarEstado(Long id, String nuevoEstado);
    List<EspacioDTO> listarPorSector(String sector);
}
