package com.campuslands.scgo.controller;

import com.campuslands.scgo.dto.IncidenciaRequestDTO;
import com.campuslands.scgo.dto.IncidenciaResponseDTO;
import com.campuslands.scgo.dto.TriajeIaLogDTO;
import com.campuslands.scgo.service.IncidenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidencias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    /**
     * Reportar incidencia con Triaje Inteligente y Asistencia de IA (Módulo 3)
     */
    @PostMapping
    public ResponseEntity<IncidenciaResponseDTO> reportarIncidencia(@Valid @RequestBody IncidenciaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenciaService.reportarIncidencia(dto));
    }

    @GetMapping
    public ResponseEntity<List<IncidenciaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(incidenciaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidenciaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(incidenciaService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<IncidenciaResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado,
            @RequestParam(required = false) Long agenteId) {
        return ResponseEntity.ok(incidenciaService.cambiarEstado(id, nuevoEstado, agenteId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<IncidenciaResponseDTO>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(incidenciaService.listarPorEstado(estado));
    }

    /**
     * Auditoría inmutable de IA: Muestra los últimos logs de evaluación de IA
     * para verificar prompts, latencia, confianza y respuestas sin datos hardcodeados.
     */
    @GetMapping("/logs-ia")
    public ResponseEntity<List<TriajeIaLogDTO>> listarLogsAuditoriaIa() {
        return ResponseEntity.ok(incidenciaService.listarLogsAuditoria());
    }
}
