package com.campuslands.scgo.service.impl;

import com.campuslands.scgo.dto.EspacioCoordenadasDTO;
import com.campuslands.scgo.dto.EspacioDTO;
import com.campuslands.scgo.exception.BusinessException;
import com.campuslands.scgo.exception.ErrorCode;
import com.campuslands.scgo.exception.ResourceNotFoundException;
import com.campuslands.scgo.model.Espacio;
import com.campuslands.scgo.repository.EspacioRepository;
import com.campuslands.scgo.service.EspacioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspacioServiceImpl implements EspacioService {

    private final EspacioRepository espacioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EspacioDTO> listarTodos() {
        return espacioRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EspacioDTO obtenerPorId(Long id) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado con id: " + id));
        return toDTO(espacio);
    }

    @Override
    @Transactional
    public EspacioDTO crear(EspacioDTO dto) {
        if (espacioRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA, "Ya existe un espacio con el nombre: " + dto.getNombre());
        }

        Espacio espacio = Espacio.builder()
                .nombre(dto.getNombre())
                .tipo(dto.getTipo())
                .aforoMaximo(dto.getAforoMaximo())
                .estado(dto.getEstado() != null ? dto.getEstado() : "disponible")
                .sector(dto.getSector() != null ? dto.getSector() : "Cabo Canaveral")
                .piso(dto.getPiso() != null ? dto.getPiso() : "Piso 1")
                .coordenadaX(dto.getCoordenadaX())
                .coordenadaY(dto.getCoordenadaY())
                .ubicacion(dto.getUbicacion())
                .build();

        return toDTO(espacioRepository.save(espacio));
    }

    @Override
    @Transactional
    public EspacioDTO actualizar(Long id, EspacioDTO dto) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado con id: " + id));

        if (!espacio.getNombre().equalsIgnoreCase(dto.getNombre()) && espacioRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA, "Ya existe un espacio con el nombre: " + dto.getNombre());
        }

        espacio.setNombre(dto.getNombre());
        espacio.setTipo(dto.getTipo());
        espacio.setAforoMaximo(dto.getAforoMaximo());
        if (dto.getEstado() != null) {
            espacio.setEstado(dto.getEstado());
        }
        if (dto.getSector() != null) {
            espacio.setSector(dto.getSector());
        }
        if (dto.getPiso() != null) {
            espacio.setPiso(dto.getPiso());
        }
        if (dto.getCoordenadaX() != null) {
            espacio.setCoordenadaX(dto.getCoordenadaX());
        }
        if (dto.getCoordenadaY() != null) {
            espacio.setCoordenadaY(dto.getCoordenadaY());
        }
        if (dto.getUbicacion() != null) {
            espacio.setUbicacion(dto.getUbicacion());
        }

        return toDTO(espacioRepository.save(espacio));
    }

    @Override
    @Transactional
    public EspacioDTO actualizarCoordenadas(Long id, EspacioCoordenadasDTO dto) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado con id: " + id));

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            espacio.setNombre(dto.getNombre());
        }
        if (dto.getSector() != null && !dto.getSector().isBlank()) {
            espacio.setSector(dto.getSector());
        }
        if (dto.getPiso() != null && !dto.getPiso().isBlank()) {
            espacio.setPiso(dto.getPiso());
        }
        espacio.setCoordenadaX(dto.getCoordenadaX());
        espacio.setCoordenadaY(dto.getCoordenadaY());

        return toDTO(espacioRepository.save(espacio));
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, String nuevoEstado) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado con id: " + id));
        espacio.setEstado(nuevoEstado);
        espacioRepository.save(espacio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspacioDTO> listarPorSector(String sector) {
        return espacioRepository.findBySector(sector).stream()
                .map(this::toDTO)
                .toList();
    }

    private EspacioDTO toDTO(Espacio e) {
        return EspacioDTO.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .tipo(e.getTipo())
                .aforoMaximo(e.getAforoMaximo())
                .estado(e.getEstado())
                .sector(e.getSector())
                .piso(e.getPiso())
                .coordenadaX(e.getCoordenadaX())
                .coordenadaY(e.getCoordenadaY())
                .ubicacion(e.getUbicacion())
                .build();
    }
}
