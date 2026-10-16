package com.campuslands.scgo.service;

import com.campuslands.scgo.dto.UsuarioDTO;

import java.util.List;

public interface UsuarioService {
    List<UsuarioDTO> listarTodos();
    UsuarioDTO obtenerPorId(Long id);
    UsuarioDTO crear(UsuarioDTO dto);
    UsuarioDTO actualizar(Long id, UsuarioDTO dto);
    void cambiarEstado(Long id, String nuevoEstado);
    List<UsuarioDTO> listarPorRol(String rol);
}
