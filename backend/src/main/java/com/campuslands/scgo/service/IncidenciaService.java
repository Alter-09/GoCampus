package com.campuslands.scgo.service;

import com.campuslands.scgo.dto.IncidenciaRequestDTO;
import com.campuslands.scgo.dto.IncidenciaResponseDTO;
import com.campuslands.scgo.dto.TriajeIaLogDTO;

import java.util.List;

public interface IncidenciaService {
    IncidenciaResponseDTO reportarIncidencia(IncidenciaRequestDTO dto);
    IncidenciaResponseDTO cambiarEstado(Long id, String nuevoEstado, Long agenteId);
    IncidenciaResponseDTO obtenerPorId(Long id);
    List<IncidenciaResponseDTO> listarTodas();
    List<IncidenciaResponseDTO> listarPorEstado(String estado);
    List<TriajeIaLogDTO> listarLogsAuditoria();
}
