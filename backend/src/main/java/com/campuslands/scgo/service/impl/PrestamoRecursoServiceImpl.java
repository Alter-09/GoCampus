package com.campuslands.scgo.service.impl;

import com.campuslands.scgo.dto.DevolucionRequestDTO;
import com.campuslands.scgo.dto.PrestamoRequestDTO;
import com.campuslands.scgo.dto.PrestamoResponseDTO;
import com.campuslands.scgo.exception.BusinessException;
import com.campuslands.scgo.exception.ErrorCode;
import com.campuslands.scgo.exception.ResourceNotFoundException;
import com.campuslands.scgo.model.Incidencia;
import com.campuslands.scgo.model.PrestamoRecurso;
import com.campuslands.scgo.model.RecursoTecnologico;
import com.campuslands.scgo.model.Usuario;
import com.campuslands.scgo.repository.IncidenciaRepository;
import com.campuslands.scgo.repository.PrestamoRecursoRepository;
import com.campuslands.scgo.repository.RecursoTecnologicoRepository;
import com.campuslands.scgo.repository.UsuarioRepository;
import com.campuslands.scgo.service.PrestamoRecursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrestamoRecursoServiceImpl implements PrestamoRecursoService {

    // Constante para reto de modificación en vivo (< 3 min)
    public static final int MAX_PRESTAMOS_SIMULTANEOS = 2;
    public static final LocalTime HORA_LIMITE_DEVOLUCION = LocalTime.of(23, 0);

    private final PrestamoRecursoRepository prestamoRepository;
    private final RecursoTecnologicoRepository recursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final IncidenciaRepository incidenciaRepository;

    @Override
    @Transactional
    public PrestamoResponseDTO solicitarPrestamo(PrestamoRequestDTO dto) {
        // 1. Validar existencia y estado del usuario solicitante
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + dto.getUsuarioId()));

        if (!"activo".equalsIgnoreCase(usuario.getEstado())) {
            throw new BusinessException(ErrorCode.USER_INACTIVE_OR_SUSPENDED,
                    "El usuario se encuentra inactivo o suspendido para solicitar préstamos");
        }

        // 2. Política de morosidad (RN-PER-03): Si tiene préstamos vencidos o atrasados, bloquear
        if (prestamoRepository.tienePrestamosMorosos(usuario.getId(), LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ACTIVE_PENALTY_OVERDUE_LOAN,
                    "Usuario bloqueado para préstamos: posee elementos con devolución vencida o en mora");
        }

        // 3. Límite de recursos activos simultáneos (RN-PER-04: Máximo 2 por persona)
        long prestamosActivos = prestamoRepository.countByUsuarioIdAndEstado(usuario.getId(), "activo");
        if (prestamosActivos >= MAX_PRESTAMOS_SIMULTANEOS) {
            throw new BusinessException(ErrorCode.LOAN_LIMIT_EXCEEDED,
                    String.format("El usuario ha alcanzado el límite máximo de %d recursos tecnológicos en préstamo simultáneo",
                            MAX_PRESTAMOS_SIMULTANEOS));
        }

        // 4. Validar existencia y disponibilidad del recurso (RN-PER-01)
        RecursoTecnologico recurso = recursoRepository.findById(dto.getRecursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Recurso tecnológico no encontrado con id: " + dto.getRecursoId()));

        if (!"operativo".equalsIgnoreCase(recurso.getEstado())) {
            throw new BusinessException(ErrorCode.RESOURCE_UNAVAILABLE,
                    String.format("El recurso '%s' (%s) no está operativo para préstamo (Estado: %s)",
                            recurso.getNombre(), recurso.getCodigoInventario(), recurso.getEstado()));
        }

        // 5. Validar fecha de devolución estimada: debe ser hoy dentro de la franja operativa
        LocalDateTime ahora = LocalDateTime.now();
        if (dto.getFechaDevolucionEstimada().isBefore(ahora)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA,
                    "La fecha de devolución estimada no puede ser en el pasado");
        }

        if (!dto.getFechaDevolucionEstimada().toLocalDate().isEqual(LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_RETURN_TIME,
                    "Los préstamos de periféricos deben devolverse el mismo día de entrega");
        }

        if (dto.getFechaDevolucionEstimada().toLocalTime().isAfter(HORA_LIMITE_DEVOLUCION)) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_RETURN_TIME,
                    "La hora máxima de devolución de periféricos es a las 11:00 PM (23:00)");
        }

        // 6. Cambiar estado del recurso a 'en_prestamo'
        recurso.setEstado("en_prestamo");
        recursoRepository.save(recurso);

        // 7. Crear el registro del préstamo
        PrestamoRecurso prestamo = PrestamoRecurso.builder()
                .recurso(recurso)
                .usuario(usuario)
                .fechaPrestamo(ahora)
                .fechaDevolucionEstimada(dto.getFechaDevolucionEstimada())
                .estado("activo")
                .observaciones(dto.getObservaciones())
                .build();

        return toDTO(prestamoRepository.save(prestamo));
    }

    @Override
    @Transactional
    public PrestamoResponseDTO registrarDevolucion(Long prestamoId, DevolucionRequestDTO dto) {
        PrestamoRecurso prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con id: " + prestamoId));

        if (!"activo".equalsIgnoreCase(prestamo.getEstado()) && !"atrasado".equalsIgnoreCase(prestamo.getEstado())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA,
                    "El préstamo ya se encuentra finalizado con estado: " + prestamo.getEstado());
        }

        LocalDateTime ahora = LocalDateTime.now();
        prestamo.setFechaDevolucionReal(ahora);

        RecursoTecnologico recurso = prestamo.getRecurso();

        if ("danado".equalsIgnoreCase(dto.getEstadoEntrega())) {
            // El periférico fue devuelto averiado
            prestamo.setEstado("dañado");
            recurso.setEstado("mantenimiento");

            String obs = dto.getObservaciones() != null ? dto.getObservaciones() : "Periférico devuelto con daño físico reportado";
            prestamo.setObservaciones((prestamo.getObservaciones() != null ? prestamo.getObservaciones() + " | " : "") + "Devolución con daño: " + obs);

            // Generar automáticamente incidencia técnica (Módulo 3)
            Incidencia incidencia = Incidencia.builder()
                    .titulo("Daño reportado en devolución: " + recurso.getNombre())
                    .descripcion("El periférico con código " + recurso.getCodigoInventario() + " fue entregado con daño reportado: " + obs)
                    .usuario(prestamo.getUsuario())
                    .recurso(recurso)
                    .prioridad("alta")
                    .categoria("tecnologia")
                    .estado("reportada")
                    .build();
            incidenciaRepository.save(incidencia);
        } else {
            // Devolución óptima
            prestamo.setEstado("devuelto");
            recurso.setEstado("operativo");
            if (dto.getObservaciones() != null && !dto.getObservaciones().isBlank()) {
                prestamo.setObservaciones((prestamo.getObservaciones() != null ? prestamo.getObservaciones() + " | " : "") + dto.getObservaciones());
            }
        }

        recursoRepository.save(recurso);
        return toDTO(prestamoRepository.save(prestamo));
    }

    @Override
    @Transactional(readOnly = true)
    public PrestamoResponseDTO obtenerPorId(Long id) {
        PrestamoRecurso prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con id: " + id));
        return toDTO(prestamo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> listarTodos() {
        return prestamoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> listarActivos() {
        return prestamoRepository.findByEstado("activo").stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> listarPorUsuario(Long usuarioId) {
        return prestamoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDTO)
                .toList();
    }

    private PrestamoResponseDTO toDTO(PrestamoRecurso p) {
        return PrestamoResponseDTO.builder()
                .id(p.getId())
                .recursoId(p.getRecurso().getId())
                .recursoNombre(p.getRecurso().getNombre())
                .recursoCodigoInventario(p.getRecurso().getCodigoInventario())
                .recursoCategoria(p.getRecurso().getCategoria())
                .usuarioId(p.getUsuario().getId())
                .usuarioNombre(p.getUsuario().getNombre())
                .usuarioEmail(p.getUsuario().getEmail())
                .fechaPrestamo(p.getFechaPrestamo())
                .fechaDevolucionEstimada(p.getFechaDevolucionEstimada())
                .fechaDevolucionReal(p.getFechaDevolucionReal())
                .estado(p.getEstado())
                .observaciones(p.getObservaciones())
                .build();
    }
}
