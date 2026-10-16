package com.campuslands.scgo.config;

import com.campuslands.scgo.model.Espacio;
import com.campuslands.scgo.model.RecursoTecnologico;
import com.campuslands.scgo.model.Usuario;
import com.campuslands.scgo.repository.EspacioRepository;
import com.campuslands.scgo.repository.RecursoTecnologicoRepository;
import com.campuslands.scgo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EspacioRepository espacioRepository;
    private final RecursoTecnologicoRepository recursoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) {
        inicializarUsuarios();
        inicializarEspacios();
        inicializarRecursos();
    }

    private void inicializarUsuarios() {
        if (usuarioRepository.count() == 0) {
            log.info("Sembrando usuarios iniciales con roles oficiales...");
            List<Usuario> usuarios = List.of(
                    Usuario.builder().nombre("Carlos Admin").email("admin@campuslands.com").documentoIdentidad("1098000001").telefono("3001234567").rol("administrador").estado("activo").build(),
                    Usuario.builder().nombre("Jhon Trainer").email("trainer.jhon@campuslands.com").documentoIdentidad("1098000002").telefono("3001234568").rol("trainer").estado("activo").build(),
                    Usuario.builder().nombre("Maria Trainer").email("trainer.maria@campuslands.com").documentoIdentidad("1098000003").telefono("3001234569").rol("trainer").estado("activo").build(),
                    Usuario.builder().nombre("Sofia Soporte").email("soporte.sofia@campuslands.com").documentoIdentidad("1098000004").telefono("3001234570").rol("soporte").estado("activo").build(),
                    Usuario.builder().nombre("David Soporte").email("soporte.david@campuslands.com").documentoIdentidad("1098000005").telefono("3001234571").rol("soporte").estado("activo").build(),
                    Usuario.builder().nombre("Andres Estudiante").email("andres.camper@campuslands.com").documentoIdentidad("1098000006").telefono("3001234572").rol("estudiante").estado("activo").build(),
                    Usuario.builder().nombre("Laura Estudiante").email("laura.camper@campuslands.com").documentoIdentidad("1098000007").telefono("3001234573").rol("estudiante").estado("activo").build(),
                    Usuario.builder().nombre("Pedro Moroso").email("pedro.sancionado@campuslands.com").documentoIdentidad("1098000008").telefono("3001234574").rol("estudiante").estado("activo").build()
            );
            usuarioRepository.saveAll(usuarios);
        }
    }

    private void inicializarEspacios() {
        if (espacioRepository.count() == 0) {
            log.info("Sembrando espacios y aforos oficiales de Campuslands con coordenadas de Minimapa...");
            List<Espacio> espacios = List.of(
                    Espacio.builder().nombre("Auditorio").tipo("auditorio").aforoMaximo(100).estado("disponible").sector("Principal").piso("Piso 1").coordenadaX(new BigDecimal("50.0")).coordenadaY(new BigDecimal("10.0")).ubicacion("Edificio Central - Nivel 1").build(),
                    Espacio.builder().nombre("Cosmos").tipo("aula").aforoMaximo(25).estado("disponible").sector("Cabo Canaveral").piso("Piso 1").coordenadaX(new BigDecimal("65.8")).coordenadaY(new BigDecimal("36.8")).ubicacion("Sector Cabo Cañaveral - Bloque A").build(),
                    Espacio.builder().nombre("Skylab").tipo("aula").aforoMaximo(20).estado("disponible").sector("Cabo Canaveral").piso("Piso 1").coordenadaX(new BigDecimal("60.5")).coordenadaY(new BigDecimal("36.6")).ubicacion("Sector Cabo Cañaveral - Bloque B").build(),
                    Espacio.builder().nombre("Sputnik").tipo("aula").aforoMaximo(38).estado("disponible").sector("ISS").piso("Piso 2").coordenadaX(new BigDecimal("25.0")).coordenadaY(new BigDecimal("45.0")).ubicacion("Estación Espacial Internacional - Nave 1").build(),
                    Espacio.builder().nombre("Apollo").tipo("aula").aforoMaximo(34).estado("disponible").sector("ISS").piso("Piso 2").coordenadaX(new BigDecimal("35.0")).coordenadaY(new BigDecimal("50.0")).ubicacion("Estación Espacial Internacional - Nave 2").build(),
                    Espacio.builder().nombre("Artemis").tipo("aula").aforoMaximo(32).estado("disponible").sector("ISS").piso("Piso 2").coordenadaX(new BigDecimal("45.0")).coordenadaY(new BigDecimal("55.0")).ubicacion("Estación Espacial Internacional - Nave 3").build(),
                    Espacio.builder().nombre("Sala blanca").tipo("aula").aforoMaximo(12).estado("disponible").sector("Cabo Canaveral").piso("Piso 1").coordenadaX(new BigDecimal("71.5")).coordenadaY(new BigDecimal("36.6")).ubicacion("Sector Cabo Cañaveral - Ala Norte").build(),
                    Espacio.builder().nombre("Sala azul").tipo("aula").aforoMaximo(10).estado("disponible").sector("Cabo Canaveral").piso("Piso 1").coordenadaX(new BigDecimal("75.0")).coordenadaY(new BigDecimal("40.0")).ubicacion("Sector Cabo Cañaveral - Ala Sur").build(),
                    Espacio.builder().nombre("Sala juntas").tipo("sala_juntas").aforoMaximo(12).estado("disponible").sector("Principal").piso("Piso 2").coordenadaX(new BigDecimal("80.0")).coordenadaY(new BigDecimal("20.0")).ubicacion("Edificio Administrativo - Nivel 2").build(),
                    Espacio.builder().nombre("Sala Amarilla/Verde 1").tipo("cubiculo").aforoMaximo(6).estado("disponible").sector("Cabo Canaveral").piso("Piso 1").coordenadaX(new BigDecimal("69.2")).coordenadaY(new BigDecimal("56.8")).ubicacion("Cabina Focus 1").build(),
                    Espacio.builder().nombre("Sala Amarilla/Verde 2").tipo("cubiculo").aforoMaximo(6).estado("disponible").sector("Cabo Canaveral").piso("Piso 1").coordenadaX(new BigDecimal("72.7")).coordenadaY(new BigDecimal("56.8")).ubicacion("Cabina Focus 2").build(),
                    Espacio.builder().nombre("Sala Amarilla/Verde 3").tipo("cubiculo").aforoMaximo(6).estado("disponible").sector("Cabo Canaveral").piso("Piso 1").coordenadaX(new BigDecimal("76.0")).coordenadaY(new BigDecimal("56.8")).ubicacion("Cabina Focus 3").build(),
                    Espacio.builder().nombre("Sala Amarilla/Verde 4").tipo("cubiculo").aforoMaximo(6).estado("disponible").sector("Cabo Canaveral").piso("Piso 1").coordenadaX(new BigDecimal("79.5")).coordenadaY(new BigDecimal("56.8")).ubicacion("Cabina Focus 4").build()
            );
            espacioRepository.saveAll(espacios);
        }
    }

    private void inicializarRecursos() {
        if (recursoRepository.count() == 0) {
            log.info("Sembrando periféricos tecnológicos autorizados...");
            List<RecursoTecnologico> recursos = List.of(
                    RecursoTecnologico.builder().codigoInventario("MOU-001").nombre("Mouse Óptico Logitech M170").categoria("mouse").estado("operativo").build(),
                    RecursoTecnologico.builder().codigoInventario("MOU-002").nombre("Mouse Ergonómico USB Dell").categoria("mouse").estado("operativo").build(),
                    RecursoTecnologico.builder().codigoInventario("MOU-003").nombre("Mouse Inalámbrico Bluetooth").categoria("mouse").estado("operativo").build(),
                    RecursoTecnologico.builder().codigoInventario("TEC-001").nombre("Teclado Mecánico Redragon").categoria("teclado").estado("operativo").build(),
                    RecursoTecnologico.builder().codigoInventario("TEC-002").nombre("Teclado Estándar Dell USB").categoria("teclado").estado("operativo").build(),
                    RecursoTecnologico.builder().codigoInventario("RED-001").nombre("Cable Ethernet RJ45 Cat6 3m").categoria("cable_red").estado("operativo").build(),
                    RecursoTecnologico.builder().codigoInventario("RED-002").nombre("Cable Ethernet RJ45 Cat6 5m").categoria("cable_red").estado("operativo").build(),
                    RecursoTecnologico.builder().codigoInventario("HDM-001").nombre("Cable HDMI 2.0 4K 2m").categoria("cable_hdmi").estado("operativo").build(),
                    RecursoTecnologico.builder().codigoInventario("HDM-002").nombre("Cable HDMI 2.0 4K 3m").categoria("cable_hdmi").estado("operativo").build(),
                    RecursoTecnologico.builder().codigoInventario("ADP-001").nombre("Adaptador USB-C a HDMI / Ethernet").categoria("adaptador").estado("operativo").build()
            );
            recursoRepository.saveAll(recursos);
        }
    }
}
