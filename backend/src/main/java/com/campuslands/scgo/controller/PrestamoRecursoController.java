package com.campuslands.scgo.controller;

import com.campuslands.scgo.dto.DevolucionRequestDTO;
import com.campuslands.scgo.dto.PrestamoRequestDTO;
import com.campuslands.scgo.dto.PrestamoResponseDTO;
import com.campuslands.scgo.service.PrestamoRecursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PrestamoRecursoController {

    private final PrestamoRecursoService prestamoService;

    @PostMapping
    public ResponseEntity<PrestamoResponseDTO> solicitarPrestamo(@Valid @RequestBody PrestamoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamoService.solicitarPrestamo(dto));
    }

    @PostMapping("/{id}/devolucion")
    public ResponseEntity<PrestamoResponseDTO> registrarDevolucion(
            @PathVariable Long id,
            @Valid @RequestBody DevolucionRequestDTO dto) {
        return ResponseEntity.ok(prestamoService.registrarDevolucion(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestamoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<PrestamoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(prestamoService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<PrestamoResponseDTO>> listarActivos() {
        return ResponseEntity.ok(prestamoService.listarActivos());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PrestamoResponseDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(prestamoService.listarPorUsuario(usuarioId));
    }
}
