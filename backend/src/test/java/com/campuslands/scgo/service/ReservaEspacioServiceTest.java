package com.campuslands.scgo.service;

import com.campuslands.scgo.dto.ReservaRequestDTO;
import com.campuslands.scgo.dto.ReservaResponseDTO;
import com.campuslands.scgo.exception.BusinessException;
import com.campuslands.scgo.exception.ErrorCode;
import com.campuslands.scgo.model.Espacio;
import com.campuslands.scgo.model.Usuario;
import com.campuslands.scgo.repository.EspacioRepository;
import com.campuslands.scgo.repository.ReservaEspacioRepository;
import com.campuslands.scgo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReservaEspacioServiceTest {

    @Autowired
    private ReservaEspacioService reservaService;

    @Autowired
    private EspacioRepository espacioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaEspacioRepository reservaRepository;

    private Usuario usuarioActivo;
    private Espacio aulaCosmos;

    @BeforeEach
    void setUp() {
        usuarioActivo = usuarioRepository.save(Usuario.builder()
                .nombre("Tester Juan")
                .email("tester.juan" + System.nanoTime() + "@campuslands.com")
                .rol("estudiante")
                .estado("activo")
                .build());

        aulaCosmos = espacioRepository.save(Espacio.builder()
                .nombre("Cosmos Test " + System.nanoTime())
                .tipo("aula")
                .aforoMaximo(25)
                .estado("disponible")
                .sector("Cabo Canaveral")
                .coordenadaX(new BigDecimal("65.8"))
                .coordenadaY(new BigDecimal("36.8"))
                .build());
    }

    @Test
    @DisplayName("Debe rechazar la reserva si la cantidad de personas supera el aforo máximo institucional")
    void testRechazoPorAforoExcedido() {
        LocalDate manana = LocalDate.now().plusDays(1);
        LocalDateTime inicio = manana.atTime(8, 0);
        LocalDateTime fin = manana.atTime(10, 0);

        ReservaRequestDTO dto = ReservaRequestDTO.builder()
                .usuarioId(usuarioActivo.getId())
                .espacioId(aulaCosmos.getId())
                .fechaInicio(inicio)
                .fechaFin(fin)
                .cantidadPersonas(30) // Aforo es 25
                .motivo("Taller masivo")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> reservaService.crearReserva(dto));
        assertEquals(ErrorCode.CAPACITY_EXCEEDED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Debe rechazar la reserva si supera las 3 horas continuas (chk_duracion_maxima)")
    void testRechazoPorDuracionMayorA3Horas() {
        LocalDate manana = LocalDate.now().plusDays(1);
        LocalDateTime inicio = manana.atTime(8, 0);
        LocalDateTime fin = manana.atTime(12, 0); // 4 horas

        ReservaRequestDTO dto = ReservaRequestDTO.builder()
                .usuarioId(usuarioActivo.getId())
                .espacioId(aulaCosmos.getId())
                .fechaInicio(inicio)
                .fechaFin(fin)
                .cantidadPersonas(20)
                .motivo("Maratón de programación")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> reservaService.crearReserva(dto));
        assertEquals(ErrorCode.DURATION_OUT_OF_BOUNDS, ex.getErrorCode());
    }

    @Test
    @DisplayName("Debe rechazar la reserva si se solicita fuera de la franja institucional (06:00 a 23:00)")
    void testRechazoFueraDeFranjaOperativa() {
        LocalDate manana = LocalDate.now().plusDays(1);
        LocalDateTime inicio = manana.atTime(5, 0); // 05:00 AM (campus abre a las 6:00)
        LocalDateTime fin = manana.atTime(7, 0);

        ReservaRequestDTO dto = ReservaRequestDTO.builder()
                .usuarioId(usuarioActivo.getId())
                .espacioId(aulaCosmos.getId())
                .fechaInicio(inicio)
                .fechaFin(fin)
                .cantidadPersonas(10)
                .motivo("Madrugón")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> reservaService.crearReserva(dto));
        assertEquals(ErrorCode.OUTSIDE_OPERATING_HOURS, ex.getErrorCode());
    }

    @Test
    @DisplayName("Debe rechazar por solapamiento si ya existe otra reserva activa para el mismo espacio")
    void testRechazoPorSolapamiento() {
        LocalDate manana = LocalDate.now().plusDays(1);
        LocalDateTime inicioExistente = manana.atTime(10, 0);
        LocalDateTime finExistente = manana.atTime(12, 0);

        // Crear primera reserva válida
        ReservaRequestDTO primera = ReservaRequestDTO.builder()
                .usuarioId(usuarioActivo.getId())
                .espacioId(aulaCosmos.getId())
                .fechaInicio(inicioExistente)
                .fechaFin(finExistente)
                .cantidadPersonas(15)
                .motivo("Clase Java")
                .build();
        reservaService.crearReserva(primera);

        // Intentar crear segunda reserva que se solapa (11:00 a 13:00)
        ReservaRequestDTO segunda = ReservaRequestDTO.builder()
                .usuarioId(usuarioActivo.getId())
                .espacioId(aulaCosmos.getId())
                .fechaInicio(manana.atTime(11, 0))
                .fechaFin(manana.atTime(13, 0))
                .cantidadPersonas(10)
                .motivo("Clase React")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> reservaService.crearReserva(segunda));
        assertEquals(ErrorCode.OVERLAPPING_RESERVATION, ex.getErrorCode());
    }

    @Test
    @DisplayName("Debe crear la reserva con éxito cuando cumple todas las reglas de negocio")
    void testCrearReservaExitosa() {
        LocalDate manana = LocalDate.now().plusDays(1);
        LocalDateTime inicio = manana.atTime(14, 0);
        LocalDateTime fin = manana.atTime(16, 0); // 2 horas, dentro de aforo y franja

        ReservaRequestDTO dto = ReservaRequestDTO.builder()
                .usuarioId(usuarioActivo.getId())
                .espacioId(aulaCosmos.getId())
                .fechaInicio(inicio)
                .fechaFin(fin)
                .cantidadPersonas(22)
                .motivo("Sesión Bootcamp")
                .build();

        ReservaResponseDTO respuesta = reservaService.crearReserva(dto);
        assertNotNull(respuesta.getId());
        assertEquals("activa", respuesta.getEstado());
        assertEquals(22, respuesta.getCantidadPersonas());
        assertEquals(aulaCosmos.getId(), respuesta.getEspacioId());
    }
}
