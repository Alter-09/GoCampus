package com.campuslands.scgo.controller;

import com.campuslands.scgo.dto.CalendarioEventoDTO;
import com.campuslands.scgo.dto.EspacioMinimapaDTO;
import com.campuslands.scgo.dto.ReservaRequestDTO;
import com.campuslands.scgo.dto.ReservaResponseDTO;
import com.campuslands.scgo.service.ReservaEspacioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReservaEspacioController {

    private final ReservaEspacioService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> crearReserva(@Valid @RequestBody ReservaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crearReserva(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(
            @PathVariable Long id,
            @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(reservaService.cancelarReserva(id, usuarioId));
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(reservaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ReservaResponseDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(reservaService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/espacio/{espacioId}")
    public ResponseEntity<List<ReservaResponseDTO>> listarPorEspacio(@PathVariable Long espacioId) {
        return ResponseEntity.ok(reservaService.listarPorEspacio(espacioId));
    }

    /**
     * Endpoint para Vista de Calendario Semanal por Usuario (Estudiante o Trainer)
     */
    @GetMapping("/calendario/usuario/{usuarioId}")
    public ResponseEntity<List<CalendarioEventoDTO>> obtenerCalendarioUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reservaService.obtenerCalendarioSemanalUsuario(usuarioId, fecha));
    }

    /**
     * Endpoint para Vista de Calendario Semanal por Espacio (Aulas y Auditorio)
     */
    @GetMapping("/calendario/espacio/{espacioId}")
    public ResponseEntity<List<CalendarioEventoDTO>> obtenerCalendarioEspacio(
            @PathVariable Long espacioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reservaService.obtenerCalendarioSemanalEspacio(espacioId, fecha));
    }

    /**
     * Dinámica del Minimapa Interactivo en Tiempo Real (Requisito Front/text.txt):
     * Retorna todos los espacios físicos del campus con sus coordenadas X/Y y marca
     * en 'ocupado = true' (color rojo) aquellos con clase/reserva en esa hora seleccionada.
     */
    @GetMapping("/minimapa")
    public ResponseEntity<List<EspacioMinimapaDTO>> obtenerOcupacionMinimapa(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora) {
        return ResponseEntity.ok(reservaService.obtenerOcupacionMinimapa(fechaHora));
    }
}
