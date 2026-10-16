package com.campuslands.scgo.controller;

import com.campuslands.scgo.dto.EspacioCoordenadasDTO;
import com.campuslands.scgo.dto.EspacioDTO;
import com.campuslands.scgo.service.EspacioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/espacios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EspacioController {

    private final EspacioService espacioService;

    @GetMapping
    public ResponseEntity<List<EspacioDTO>> listarTodos() {
        return ResponseEntity.ok(espacioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspacioDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(espacioService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<EspacioDTO> crear(@Valid @RequestBody EspacioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(espacioService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspacioDTO> actualizar(@PathVariable Long id, @Valid @RequestBody EspacioDTO dto) {
        return ResponseEntity.ok(espacioService.actualizar(id, dto));
    }

    /**
     * Panel de Administración de Nombres y Señalamiento (Requisito Front/text.txt):
     * Módulo exclusivo para administradores para reconfigurar coordenadas y nombres en el Minimapa.
     */
    @PatchMapping("/{id}/coordenadas")
    public ResponseEntity<EspacioDTO> actualizarCoordenadas(
            @PathVariable Long id,
            @Valid @RequestBody EspacioCoordenadasDTO dto) {
        return ResponseEntity.ok(espacioService.actualizarCoordenadas(id, dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestParam String nuevoEstado) {
        espacioService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sector/{sector}")
    public ResponseEntity<List<EspacioDTO>> listarPorSector(@PathVariable String sector) {
        return ResponseEntity.ok(espacioService.listarPorSector(sector));
    }
}
