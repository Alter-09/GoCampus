package com.campuslands.scgo.service.impl;

import com.campuslands.scgo.dto.UsuarioDTO;
import com.campuslands.scgo.exception.BusinessException;
import com.campuslands.scgo.exception.ErrorCode;
import com.campuslands.scgo.exception.ResourceNotFoundException;
import com.campuslands.scgo.model.Usuario;
import com.campuslands.scgo.repository.UsuarioRepository;
import com.campuslands.scgo.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        return toDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioDTO crear(UsuarioDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA, "Ya existe un usuario con el email: " + dto.getEmail());
        }

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .documentoIdentidad(dto.getDocumentoIdentidad())
                .telefono(dto.getTelefono())
                .rol(dto.getRol())
                .estado(dto.getEstado() != null ? dto.getEstado() : "activo")
                .build();

        return toDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        if (!usuario.getEmail().equalsIgnoreCase(dto.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA, "El email " + dto.getEmail() + " ya está en uso por otro usuario");
        }

        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setDocumentoIdentidad(dto.getDocumentoIdentidad());
        usuario.setTelefono(dto.getTelefono());
        usuario.setRol(dto.getRol());
        if (dto.getEstado() != null) {
            usuario.setEstado(dto.getEstado());
        }

        return toDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, String nuevoEstado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setEstado(nuevoEstado);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarPorRol(String rol) {
        return usuarioRepository.findByRol(rol).stream()
                .map(this::toDTO)
                .toList();
    }

    private UsuarioDTO toDTO(Usuario u) {
        return UsuarioDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .documentoIdentidad(u.getDocumentoIdentidad())
                .telefono(u.getTelefono())
                .rol(u.getRol())
                .estado(u.getEstado())
                .fechaCreacion(u.getFechaCreacion())
                .build();
    }
}
