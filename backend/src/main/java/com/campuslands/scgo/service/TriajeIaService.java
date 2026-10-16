package com.campuslands.scgo.service;

import com.campuslands.scgo.dto.TriajeIaResultadoDTO;

public interface TriajeIaService {
    TriajeIaResultadoDTO evaluarIncidencia(String titulo, String descripcion, String espacioNombre, String recursoNombre);
}
