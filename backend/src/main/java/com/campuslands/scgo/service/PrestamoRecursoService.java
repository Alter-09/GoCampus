package com.campuslands.scgo.service;

import com.campuslands.scgo.dto.DevolucionRequestDTO;
import com.campuslands.scgo.dto.PrestamoRequestDTO;
import com.campuslands.scgo.dto.PrestamoResponseDTO;

import java.util.List;

public interface PrestamoRecursoService {
    PrestamoResponseDTO solicitarPrestamo(PrestamoRequestDTO dto);
    PrestamoResponseDTO registrarDevolucion(Long prestamoId, DevolucionRequestDTO dto);
    PrestamoResponseDTO obtenerPorId(Long id);
    List<PrestamoResponseDTO> listarTodos();
    List<PrestamoResponseDTO> listarActivos();
    List<PrestamoResponseDTO> listarPorUsuario(Long usuarioId);
}
