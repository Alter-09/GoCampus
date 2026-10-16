package com.campuslands.scgo.service.impl;

import com.campuslands.scgo.dto.RecursoDTO;
import com.campuslands.scgo.exception.BusinessException;
import com.campuslands.scgo.exception.ErrorCode;
import com.campuslands.scgo.exception.ResourceNotFoundException;
import com.campuslands.scgo.model.Espacio;
import com.campuslands.scgo.model.RecursoTecnologico;
import com.campuslands.scgo.repository.EspacioRepository;
import com.campuslands.scgo.repository.RecursoTecnologicoRepository;
import com.campuslands.scgo.service.RecursoTecnologicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecursoTecnologicoServiceImpl implements RecursoTecnologicoService {

    private final RecursoTecnologicoRepository recursoRepository;
    private final EspacioRepository espacioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RecursoDTO> listarTodos() {
        return recursoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RecursoDTO obtenerPorId(Long id) {
        RecursoTecnologico r = recursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado con id: " + id));
        return toDTO(r);
    }

    @Override
    @Transactional
    public RecursoDTO crear(RecursoDTO dto) {
        if (recursoRepository.existsByCodigoInventario(dto.getCodigoInventario())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA,
                    "Ya existe un recurso con el código de inventario: " + dto.getCodigoInventario());
        }

        Espacio espacio = null;
        if (dto.getEspacioAsignadoId() != null) {
            espacio = espacioRepository.findById(dto.getEspacioAsignadoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Espacio asignado no encontrado con id: " + dto.getEspacioAsignadoId()));
        }

        RecursoTecnologico r = RecursoTecnologico.builder()
                .codigoInventario(dto.getCodigoInventario())
                .nombre(dto.getNombre())
                .categoria(dto.getCategoria())
                .estado(dto.getEstado() != null ? dto.getEstado() : "operativo")
                .espacioAsignado(espacio)
                .build();

        return toDTO(recursoRepository.save(r));
    }

    @Override
    @Transactional
    public RecursoDTO actualizar(Long id, RecursoDTO dto) {
        RecursoTecnologico r = recursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado con id: " + id));

        if (!r.getCodigoInventario().equalsIgnoreCase(dto.getCodigoInventario())
                && recursoRepository.existsByCodigoInventario(dto.getCodigoInventario())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA,
                    "Ya existe otro recurso con el código de inventario: " + dto.getCodigoInventario());
        }

        r.setCodigoInventario(dto.getCodigoInventario());
        r.setNombre(dto.getNombre());
        r.setCategoria(dto.getCategoria());
        if (dto.getEstado() != null) {
            r.setEstado(dto.getEstado());
        }

        if (dto.getEspacioAsignadoId() != null) {
            Espacio espacio = espacioRepository.findById(dto.getEspacioAsignadoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado con id: " + dto.getEspacioAsignadoId()));
            r.setEspacioAsignado(espacio);
        } else {
            r.setEspacioAsignado(null);
        }

        return toDTO(recursoRepository.save(r));
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, String nuevoEstado) {
        RecursoTecnologico r = recursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado con id: " + id));
        r.setEstado(nuevoEstado);
        recursoRepository.save(r);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecursoDTO> listarPorCategoria(String categoria) {
        return recursoRepository.findByCategoria(categoria).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecursoDTO> listarDisponiblesParaPrestamo() {
        return recursoRepository.findByEstado("operativo").stream()
                .map(this::toDTO)
                .toList();
    }

    private RecursoDTO toDTO(RecursoTecnologico r) {
        return RecursoDTO.builder()
                .id(r.getId())
                .codigoInventario(r.getCodigoInventario())
                .nombre(r.getNombre())
                .categoria(r.getCategoria())
                .estado(r.getEstado())
                .espacioAsignadoId(r.getEspacioAsignado() != null ? r.getEspacioAsignado().getId() : null)
                .espacioAsignadoNombre(r.getEspacioAsignado() != null ? r.getEspacioAsignado().getNombre() : null)
                .fechaIngreso(r.getFechaIngreso())
                .build();
    }
}
