package com.campuslands.scgo.service.impl;

import com.campuslands.scgo.dto.*;
import com.campuslands.scgo.exception.BusinessException;
import com.campuslands.scgo.exception.ErrorCode;
import com.campuslands.scgo.exception.ResourceNotFoundException;
import com.campuslands.scgo.model.Espacio;
import com.campuslands.scgo.model.ReservaEspacio;
import com.campuslands.scgo.model.Usuario;
import com.campuslands.scgo.repository.EspacioRepository;
import com.campuslands.scgo.repository.ReservaEspacioRepository;
import com.campuslands.scgo.repository.UsuarioRepository;
import com.campuslands.scgo.service.ReservaEspacioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaEspacioServiceImpl implements ReservaEspacioService {

    // Constantes de Dominio desacopladas para el Reto de Modificación en Vivo (< 3 minutos)
    public static final int MIN_DURATION_MINUTES = 15;
    public static final int MAX_DURATION_MINUTES = 180; // 3 Horas continuas (text.txt y DDL)
    public static final LocalTime HORA_APERTURA_CAMPUS = LocalTime.of(6, 0); // 06:00 AM
    public static final LocalTime HORA_CIERRE_CAMPUS = LocalTime.of(23, 0);   // 11:00 PM

    private final ReservaEspacioRepository reservaEspacioRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public ReservaResponseDTO crearReserva(ReservaRequestDTO dto) {
        // 0. Normalizar fecha de reserva y horas si fueron enviadas en campos separados
        if (dto.getFechaInicio() == null) {
            LocalDate fecha = dto.getFechaReserva();
            LocalTime hora = dto.getHoraInicio() != null ? dto.getHoraInicio() : dto.getHoraReserva();
            if (fecha != null && hora != null) {
                dto.setFechaInicio(LocalDateTime.of(fecha, hora));
            } else {
                throw new BusinessException(ErrorCode.INVALID_INPUT_DATA,
                        "Debe especificar la fecha de reserva y la hora de inicio");
            }
        }

        if (dto.getFechaFin() == null) {
            LocalDate fecha = dto.getFechaReserva() != null ? dto.getFechaReserva() : dto.getFechaInicio().toLocalDate();
            LocalTime fin = dto.getHoraFin();
            if (fin != null) {
                dto.setFechaFin(LocalDateTime.of(fecha, fin));
            } else {
                // Por defecto 1 hora si no se especifica hora de fin
                dto.setFechaFin(dto.getFechaInicio().plusHours(1));
            }
        }

        // 1. Validar usuario solicitante
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + dto.getUsuarioId()));

        if (!"activo".equalsIgnoreCase(usuario.getEstado())) {
            throw new BusinessException(ErrorCode.USER_INACTIVE_OR_SUSPENDED,
                    "El usuario con id " + dto.getUsuarioId() + " no está activo para realizar reservas");
        }

        // 2. Validar espacio solicitado
        Espacio espacio = espacioRepository.findById(dto.getEspacioId())
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado con id: " + dto.getEspacioId()));

        if (!"disponible".equalsIgnoreCase(espacio.getEstado())) {
            throw new BusinessException(ErrorCode.SPACE_NOT_AVAILABLE,
                    "El espacio '" + espacio.getNombre() + "' no está disponible (Estado actual: " + espacio.getEstado() + ")");
        }

        // 3. Validar coherencia de fechas (fecha_fin > fecha_inicio)
        if (!dto.getFechaFin().isAfter(dto.getFechaInicio())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA,
                    "La fecha y hora de finalización debe ser posterior a la de inicio");
        }

        // 4. Validar fecha en el pasado
        if (dto.getFechaInicio().isBefore(LocalDateTime.now().minusMinutes(2))) {
            throw new BusinessException(ErrorCode.RESERVATION_IN_PAST,
                    "No se permiten reservas con fechas u horas en el pasado");
        }

        // 5. Validar integridad de mismo día (no pernocta ni cruce de medianoche)
        if (!dto.getFechaInicio().toLocalDate().isEqual(dto.getFechaFin().toLocalDate())) {
            throw new BusinessException(ErrorCode.OVERNIGHT_NOT_ALLOWED,
                    "Las reservas deben iniciar y finalizar en la misma fecha (no se permite pernocta)");
        }

        // 6. Validar franja institucional (6:00 AM a 11:00 PM de lunes a domingo)
        LocalTime horaInicio = dto.getFechaInicio().toLocalTime();
        LocalTime horaFin = dto.getFechaFin().toLocalTime();

        if (horaInicio.isBefore(HORA_APERTURA_CAMPUS) || horaFin.isAfter(HORA_CIERRE_CAMPUS)) {
            throw new BusinessException(ErrorCode.OUTSIDE_OPERATING_HOURS,
                    "El campus opera exclusivamente de 6:00 AM a 11:00 PM. Horario solicitado fuera de rango.");
        }

        // 7. Validar restricción de duración (15 min a 3 horas continuas)
        Duration duracion = Duration.between(dto.getFechaInicio(), dto.getFechaFin());
        long minutosTotales = duracion.toMinutes();

        if (minutosTotales < MIN_DURATION_MINUTES || minutosTotales > MAX_DURATION_MINUTES) {
            throw new BusinessException(ErrorCode.DURATION_OUT_OF_BOUNDS,
                    String.format("La duración de la reserva debe estar entre %d minutos y %d minutos (3 horas continuas). Duración solicitada: %d minutos.",
                            MIN_DURATION_MINUTES, MAX_DURATION_MINUTES, minutosTotales));
        }

        // 8. Validar control estricto de aforo
        if (dto.getCantidadPersonas() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA, "La cantidad de personas debe ser mayor a 0");
        }

        if (dto.getCantidadPersonas() > espacio.getAforoMaximo()) {
            throw new BusinessException(ErrorCode.CAPACITY_EXCEEDED,
                    String.format("La cantidad de personas solicitada (%d) supera el aforo máximo permitido (%d) para el espacio '%s'",
                            dto.getCantidadPersonas(), espacio.getAforoMaximo(), espacio.getNombre()));
        }

        // 9. Validar solapamiento de horarios (Backend Overlap Validation)
        boolean solapa = reservaEspacioRepository.existeSolapamiento(
                espacio.getId(),
                dto.getFechaInicio(),
                dto.getFechaFin(),
                null
        );

        if (solapa) {
            throw new BusinessException(ErrorCode.OVERLAPPING_RESERVATION,
                    String.format("Conflicto de horario: El espacio '%s' ya se encuentra reservado en el intervalo solicitado.",
                            espacio.getNombre()));
        }

        // 10. Persistir la reserva
        ReservaEspacio reserva = ReservaEspacio.builder()
                .espacio(espacio)
                .usuario(usuario)
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .cantidadPersonas(dto.getCantidadPersonas())
                .motivo(dto.getMotivo())
                .estado("activa")
                .build();

        return toDTO(reservaEspacioRepository.save(reserva));
    }

    @Override
    @Transactional
    public ReservaResponseDTO cancelarReserva(Long id, Long usuarioId) {
        ReservaEspacio reserva = reservaEspacioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + id));

        if (usuarioId != null && !reserva.getUsuario().getId().equals(usuarioId)) {
            Usuario user = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));
            if (!"administrador".equalsIgnoreCase(user.getRol())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_DATA, "No tienes permisos para cancelar una reserva de otro usuario");
            }
        }

        reserva.setEstado("cancelada");
        return toDTO(reservaEspacioRepository.save(reserva));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponseDTO obtenerPorId(Long id) {
        ReservaEspacio reserva = reservaEspacioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + id));
        return toDTO(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarTodas() {
        return reservaEspacioRepository.findAll().stream()
                .filter(r -> "activa".equalsIgnoreCase(r.getEstado()) || "en_curso".equalsIgnoreCase(r.getEstado()))
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarPorUsuario(Long usuarioId) {
        return reservaEspacioRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarPorEspacio(Long espacioId) {
        return reservaEspacioRepository.findByEspacioIdAndEstado(espacioId, "activa").stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioEventoDTO> obtenerCalendarioSemanalUsuario(Long usuarioId, LocalDate fecha) {
        LocalDate base = (fecha != null) ? fecha : LocalDate.now();
        LocalDate lunes = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = base.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime desde = lunes.atTime(0, 0, 0);
        LocalDateTime hasta = domingo.atTime(23, 59, 59);

        return reservaEspacioRepository.findByUsuarioIdAndRango(usuarioId, desde, hasta).stream()
                .map(this::toEventoDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioEventoDTO> obtenerCalendarioSemanalEspacio(Long espacioId, LocalDate fecha) {
        LocalDate base = (fecha != null) ? fecha : LocalDate.now();
        LocalDate lunes = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingo = base.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime desde = lunes.atTime(0, 0, 0);
        LocalDateTime hasta = domingo.atTime(23, 59, 59);

        return reservaEspacioRepository.findByEspacioIdAndRango(espacioId, desde, hasta).stream()
                .map(this::toEventoDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspacioMinimapaDTO> obtenerOcupacionMinimapa(LocalDateTime fechaHora) {
        LocalDateTime momento = (fechaHora != null) ? fechaHora : LocalDateTime.now();
        List<Espacio> espacios = espacioRepository.findAll();

        return espacios.stream().map(espacio -> {
            List<ReservaEspacio> reservasEnMomento = reservaEspacioRepository.findByEspacioIdAndRango(
                    espacio.getId(),
                    momento.minusSeconds(1),
                    momento.plusSeconds(1)
            );

            boolean ocupado = !reservasEnMomento.isEmpty();
            Long reservaId = ocupado ? reservasEnMomento.getFirst().getId() : null;
            String evento = ocupado ? (reservasEnMomento.getFirst().getMotivo() != null ? reservasEnMomento.getFirst().getMotivo() : "Clase en curso") : null;

            return EspacioMinimapaDTO.builder()
                    .id(espacio.getId())
                    .nombre(espacio.getNombre())
                    .tipo(espacio.getTipo())
                    .aforoMaximo(espacio.getAforoMaximo())
                    .sector(espacio.getSector())
                    .piso(espacio.getPiso())
                    .coordenadaX(espacio.getCoordenadaX())
                    .coordenadaY(espacio.getCoordenadaY())
                    .ubicacion(espacio.getUbicacion())
                    .ocupado(ocupado)
                    .reservaActualId(reservaId)
                    .eventoTitulo(evento)
                    .estadoEspacio(espacio.getEstado())
                    .build();
        }).toList();
    }

    private ReservaResponseDTO toDTO(ReservaEspacio r) {
        return ReservaResponseDTO.builder()
                .id(r.getId())
                .espacioId(r.getEspacio().getId())
                .espacioNombre(r.getEspacio().getNombre())
                .espacioAforoMaximo(r.getEspacio().getAforoMaximo())
                .usuarioId(r.getUsuario().getId())
                .usuarioNombre(r.getUsuario().getNombre())
                .usuarioEmail(r.getUsuario().getEmail())
                .fechaInicio(r.getFechaInicio())
                .fechaFin(r.getFechaFin())
                .fechaReserva(r.getFechaInicio().toLocalDate())
                .horaInicio(r.getFechaInicio().toLocalTime())
                .horaFin(r.getFechaFin().toLocalTime())
                .horaReserva(r.getFechaInicio().toLocalTime())
                .cantidadPersonas(r.getCantidadPersonas())
                .estado(r.getEstado())
                .motivo(r.getMotivo())
                .fechaRegistro(r.getFechaRegistro())
                .build();
    }

    private CalendarioEventoDTO toEventoDTO(ReservaEspacio r) {
        return CalendarioEventoDTO.builder()
                .id(r.getId())
                .title(r.getEspacio().getNombre() + " (" + (r.getMotivo() != null ? r.getMotivo() : "Reserva") + ")")
                .start(r.getFechaInicio())
                .end(r.getFechaFin())
                .espacioId(r.getEspacio().getId())
                .espacioNombre(r.getEspacio().getNombre())
                .sector(r.getEspacio().getSector())
                .usuarioId(r.getUsuario().getId())
                .usuarioNombre(r.getUsuario().getNombre())
                .estado(r.getEstado())
                .cantidadPersonas(r.getCantidadPersonas())
                .build();
    }
}
