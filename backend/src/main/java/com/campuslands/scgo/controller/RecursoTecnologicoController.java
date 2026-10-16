package com.campuslands.scgo.controller;

import com.campuslands.scgo.dto.RecursoDTO;
import com.campuslands.scgo.service.RecursoTecnologicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recursos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecursoTecnologicoController {

    private final RecursoTecnologicoService recursoService;

    @GetMapping
    public ResponseEntity<List<RecursoDTO>> listarTodos() {
        return ResponseEntity.ok(recursoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecursoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(recursoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<RecursoDTO> crear(@Valid @RequestBody RecursoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recursoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecursoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody RecursoDTO dto) {
        return ResponseEntity.ok(recursoService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestParam String nuevoEstado) {
        recursoService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<RecursoDTO>> listarPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(recursoService.listarPorCategoria(categoria));
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<RecursoDTO>> listarDisponibles() {
        return ResponseEntity.ok(recursoService.listarDisponiblesParaPrestamo());
    }
}
