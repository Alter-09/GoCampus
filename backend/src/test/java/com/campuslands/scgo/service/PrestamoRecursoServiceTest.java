package com.campuslands.scgo.service;

import com.campuslands.scgo.dto.DevolucionRequestDTO;
import com.campuslands.scgo.dto.PrestamoRequestDTO;
import com.campuslands.scgo.dto.PrestamoResponseDTO;
import com.campuslands.scgo.exception.BusinessException;
import com.campuslands.scgo.exception.ErrorCode;
import com.campuslands.scgo.model.Incidencia;
import com.campuslands.scgo.model.RecursoTecnologico;
import com.campuslands.scgo.model.Usuario;
import com.campuslands.scgo.repository.IncidenciaRepository;
import com.campuslands.scgo.repository.RecursoTecnologicoRepository;
import com.campuslands.scgo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PrestamoRecursoServiceTest {

    @Autowired
    private PrestamoRecursoService prestamoService;

    @Autowired
    private RecursoTecnologicoRepository recursoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    private Usuario usuario;
    private RecursoTecnologico mouse1;
    private RecursoTecnologico mouse2;
    private RecursoTecnologico teclado;

    @BeforeEach
    void setUp() {
        usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Estudiante Prueba")
                .email("estudiante.prueba" + System.nanoTime() + "@campuslands.com")
                .rol("estudiante")
                .estado("activo")
                .build());

        mouse1 = recursoRepository.save(RecursoTecnologico.builder()
                .codigoInventario("TEST-MOU-" + System.nanoTime())
                .nombre("Mouse Óptico 1")
                .categoria("mouse")
                .estado("operativo")
                .build());

        mouse2 = recursoRepository.save(RecursoTecnologico.builder()
                .codigoInventario("TEST-MOU-" + (System.nanoTime() + 1))
                .nombre("Mouse Óptico 2")
                .categoria("mouse")
                .estado("operativo")
                .build());

        teclado = recursoRepository.save(RecursoTecnologico.builder()
                .codigoInventario("TEST-TEC-" + (System.nanoTime() + 2))
                .nombre("Teclado Mecánico")
                .categoria("teclado")
                .estado("operativo")
                .build());
    }

    @Test
    @DisplayName("Debe rechazar si el usuario supera el límite de 2 préstamos simultáneos")
    void testRechazoPorLimite2Prestamos() {
        LocalDateTime estimada = LocalDate.now().atTime(20, 0);

        // Prestar 1
        prestamoService.solicitarPrestamo(PrestamoRequestDTO.builder()
                .usuarioId(usuario.getId())
                .recursoId(mouse1.getId())
                .fechaDevolucionEstimada(estimada)
                .build());

        // Prestar 2
        prestamoService.solicitarPrestamo(PrestamoRequestDTO.builder()
                .usuarioId(usuario.getId())
                .recursoId(mouse2.getId())
                .fechaDevolucionEstimada(estimada)
                .build());

        // Intentar un 3er préstamo
        PrestamoRequestDTO tercerPrestamo = PrestamoRequestDTO.builder()
                .usuarioId(usuario.getId())
                .recursoId(teclado.getId())
                .fechaDevolucionEstimada(estimada)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.solicitarPrestamo(tercerPrestamo));
        assertEquals(ErrorCode.LOAN_LIMIT_EXCEEDED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Debe rechazar el préstamo si el recurso se encuentra en mantenimiento o inhabilitado")
    void testRechazoRecursoNoOperativo() {
        mouse1.setEstado("mantenimiento");
        recursoRepository.save(mouse1);

        PrestamoRequestDTO dto = PrestamoRequestDTO.builder()
                .usuarioId(usuario.getId())
                .recursoId(mouse1.getId())
                .fechaDevolucionEstimada(LocalDate.now().atTime(21, 0))
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> prestamoService.solicitarPrestamo(dto));
        assertEquals(ErrorCode.RESOURCE_UNAVAILABLE, ex.getErrorCode());
    }

    @Test
    @DisplayName("Devolución con daño físico debe pasar el recurso a mantenimiento y generar incidencia técnica automática")
    void testDevolucionConDano() {
        PrestamoResponseDTO prestamo = prestamoService.solicitarPrestamo(PrestamoRequestDTO.builder()
                .usuarioId(usuario.getId())
                .recursoId(mouse1.getId())
                .fechaDevolucionEstimada(LocalDate.now().atTime(20, 0))
                .build());

        DevolucionRequestDTO devolucion = DevolucionRequestDTO.builder()
                .estadoEntrega("danado")
                .observaciones("Cable USB trozado y botón derecho roto")
                .build();

        PrestamoResponseDTO devuelto = prestamoService.registrarDevolucion(prestamo.getId(), devolucion);
        assertEquals("dañado", devuelto.getEstado());

        // Verificar que el recurso pasó a mantenimiento
        RecursoTecnologico recursoActualizado = recursoRepository.findById(mouse1.getId()).orElseThrow();
        assertEquals("mantenimiento", recursoActualizado.getEstado());

        // Verificar que se creó la incidencia técnica automática
        List<Incidencia> incidencias = incidenciaRepository.findByRecursoId(mouse1.getId());
        assertFalse(incidencias.isEmpty());
        assertEquals("alta", incidencias.getFirst().getPrioridad());
        assertEquals("tecnologia", incidencias.getFirst().getCategoria());
    }
}
