package com.campuslands.scgo.service;

import com.campuslands.scgo.dto.CalendarioEventoDTO;
import com.campuslands.scgo.dto.EspacioMinimapaDTO;
import com.campuslands.scgo.dto.ReservaRequestDTO;
import com.campuslands.scgo.dto.ReservaResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservaEspacioService {
    ReservaResponseDTO crearReserva(ReservaRequestDTO dto);
    ReservaResponseDTO cancelarReserva(Long id, Long usuarioId);
    ReservaResponseDTO obtenerPorId(Long id);
    List<ReservaResponseDTO> listarTodas();
    List<ReservaResponseDTO> listarPorUsuario(Long usuarioId);
    List<ReservaResponseDTO> listarPorEspacio(Long espacioId);
    List<CalendarioEventoDTO> obtenerCalendarioSemanalUsuario(Long usuarioId, LocalDate fecha);
    List<CalendarioEventoDTO> obtenerCalendarioSemanalEspacio(Long espacioId, LocalDate fecha);
    List<EspacioMinimapaDTO> obtenerOcupacionMinimapa(LocalDateTime fechaHora);
}
