# GoCampus - Sistema Central de Gestion Operativa (SCGO)

Sistema Central de Gestion Operativa web para la gestion integral de campus educativos y tecnologicos (Campuslands). La plataforma permite la reserva de espacios fisicos respetando aforos y duraciones maximas, el control y prestamo exclusivo de perifericos tecnologicos, la visualizacion interactiva en minimapa 2D y exploracion 3D, y un modulo de triaje inteligente de incidencias asistido por Inteligencia Artificial (Grok-2 / xAI) con mecanismo de contingencia heuristica deterministica.

---

## Tabla de Contenidos

- [Integrantes del Proyecto](#integrantes-del-proyecto)
- [Descripcion General](#descripcion-general)
- [Caracteristicas Principales](#caracteristicas-principales)
  - [Modulo 1: Gestion y Reserva de Espacios](#modulo-1-gestion-y-reserva-de-espacios)
  - [Modulo 2: Control de Prestamos de Perifericos Tecnologicos](#modulo-2-control-de-prestamos-de-perifericos-tecnologicos)
  - [Modulo 3: Triaje Inteligente de Incidencias con IA](#modulo-3-triaje-inteligente-de-incidencias-con-ia)
  - [Modulo 4: Visualizador 2D Vectorial y Recorrido 3D Inmersivo](#modulo-4-visualizador-2d-vectorial-y-recorrido-3d-inmersivo)
- [Arquitectura del Sistema](#arquitectura-del-sistema)
- [Stack Tecnologico](#stack-tecnologico)
- [Estructura del Repositorio](#estructura-del-repositorio)
- [Reglas de Negocio Implementadas](#reglas-de-negocio-implementadas)
- [Requisitos Previos](#requisitos-previos)
- [Instalacion y Configuracion](#instalacion-y-configuracion)
  - [1. Clonacion del Repositorio](#1-clonacion-del-repositorio)
  - [2. Configuracion del Backend](#2-configuracion-del-backend)
  - [3. Variables de Entorno](#3-variables-de-entorno)
  - [4. Ejecucion del Backend](#4-ejecucion-del-backend)
  - [5. Ejecucion del Frontend](#5-ejecucion-del-frontend)
- [Referencia de la API REST](#referencia-de-la-api-rest)
- [Ejecucion de Pruebas](#ejecucion-de-pruebas)
- [Licencia](#licencia)

---

## Integrantes del Proyecto

- Sergio Aparicio
- Yeison Pallares
- Diego Leon
- Jhon Reyes

---

## Descripcion General

GoCampus (SCGO) resuelve la necesidad operativa de administrar de forma sincronizada, segura y automatizada la infraestructura fisica y tecnica de los campus. Centraliza en una arquitectura desacoplada:

1. El control estricto de disponibilidad y aforo de aulas, salas de juntas, auditorios y cabinas.
2. El registro riguroso de inventario y prestamo de hardware y accesorios informaticos.
3. La recepcion, clasificacion, analisis de impacto y priorizacion tecnica de fallas mediante modelos de lenguaje natural y motores de reglas.
4. La navegacion espacial del campus mediante planos 2D interactivos y un entorno WebGL 3D en primera persona con soporte de accesibilidad.

---

## Caracteristicas Principales

### Modulo 1: Gestion y Reserva de Espacios
- Control estricto de aforos por espacio fisico (auditorios, aulas tecnicas, salas focus, cabinas individuales).
- Validacion de franja operativa institucional (06:00 a 23:00, hora Colombia / Bogota).
- Restriccion de reservas nocturnas no autorizadas (prohibicion de pernocta).
- Validacion de duracion maxima por sesion (maximo 3 horas continuas).
- Algoritmo de deteccion y prevencion de solapamiento de horarios (double booking) a nivel de base de datos y logica de servicio.
- Generacion y exportacion dinamica de eventos en estandar iCalendar (.ics) para integracion con calendarios externos.
- Vistas de calendario semanal filtrables por usuario solicitante o por espacio fisico.

### Modulo 2: Control de Prestamos de Perifericos Tecnologicos
- Catalogo controlado de recursos: mouse, teclado, cable de red RJ45, cable HDMI y adaptadores multipuerto.
- Politica de asignacion unica: un usuario solo puede tener en prestamo un recurso activo por categoria simultaneamente.
- Verificacion automatica del estado del usuario (rechazo si esta suspendido, inactivo o reporta mora/sancion vigente).
- Control de tiempos de entrega y registro auditado del estado fisico del periferico al momento de la devolucion.

### Modulo 3: Triaje Inteligente de Incidencias con IA
- Evaluacion de reportes tecnicos mediante integracion directa con LLM (modelo Grok-2 / xAI via API REST compatible con OpenAI).
- Determinacion automatica de:
  - Categoria: infraestructura, tecnologia, logistica, bienestar u otro.
  - Nivel de prioridad: baja, media, alta o critica.
  - Indice numerico de confianza tecnica (0.00 a 1.00).
  - Justificacion diagnostica del riesgo operativo.
  - Accion correctiva o preventiva inmediata sugerida.
- Mecanismo de contingencia (fallback) heuristico local: si la red o el servicio de IA externo no responde, un analizador sintactico deterministico procesa la incidencia garantizando cero interrupciones en el servicio.
- Bitacora inmutable de auditoria de IA con registro de prompts, latencias de inferencia y payloads de respuesta.

### Modulo 4: Visualizador 2D Vectorial y Recorrido 3D Inmersivo
- Minimapa 2D renderizado en SVG puro con soporte para multiples sectores institucionales:
  - Sector Cabo Canaveral (Auditorio, Salas de Mentorias, Cabinas Focus, Coworking).
  - Sector Estacion Espacial Internacional / ISS (Salones Skylab, Sputnik, Apollo, Artemis).
- Web Component personalizado con Shadow DOM (`<campus-status-badge>`) para indicar estados operativos con pulsos luminicos.
- Motor 3D en Three.js con navegacion en primera persona (Pointer Lock y controles WASD/flechas).
- Soporte para preferencias de movimiento reducido (`prefers-reduced-motion`) alternando entre navegacion FPS activa y navegacion estatica accesible por teclado.
- Asistente virtual institucional interactivo ("Orbit") integrado dentro de la interfaz HUD.

---

## Arquitectura del Sistema

El proyecto esta disenado bajo una arquitectura por capas desacopladas que favorece la alta cohesion, el bajo acoplamiento y la mantenibilidad:

```
[ Cliente Web / Navegador ]
         |
         +--> Interfaz de Usuario 2D (HTML5 / CSS3 / Web Components)
         +--> Motor de Recorrido 3D (Three.js WebGL / Canvas)
         |
    (HTTP / REST JSON)
         |
         v
[ Backend Spring Boot 4.x / Java 21 ]
         |
         +--> Capa de Controladores REST (@RestController, CORS habilitado)
         +--> Capa de DTOs y Validacion (@Valid, Jakarta Bean Validation)
         +--> Capa de Servicios de Dominio (@Service, Transaccionalidad)
         |       |
         |       +--> Motor de Negocio (Reservas, Prestamos, Espacios)
         |       +--> Servicio de Triaje IA (xAI Grok-2 + Fallback Local)
         |
         +--> Capa de Persistencia Spring Data JPA / Hibernate
                 |
                 +--> Base de Datos Principal: PostgreSQL
                 +--> Base de Datos de Prueba / Perfil Rapido: H2 In-Memory
```

---

## Stack Tecnologico

### Backend
- Lenguaje: Java 21 (LTS)
- Framework: Spring Boot 4.1.1
- Modulos Spring: Spring Boot Starter Web MVC, Spring Boot Starter Data JPA, Spring Boot Starter Validation
- Persistencia y ORM: Hibernate 6.x
- Base de Datos Principal: PostgreSQL 15+
- Base de Datos de Desarrollo y Test: H2 Database
- Utilidades: Project Lombok, Jackson Databind, Java HTTP Client
- Herramienta de Construccion: Gradle con Gradle Wrapper integrado

### Frontend
- Estructura: HTML5 semantico
- Estilos: CSS3 Vanilla con variables y tokens de diseno institucional (Brandbook Campuslands)
- Logica: JavaScript Vanilla (ES6+) modular y reactivo
- Componentes Web: Web Components nativos con Shadow DOM encapsulado
- Renderizado Grafico 3D: Three.js (r128) WebGL Vanilla
- Estandar de Calendario: Generador nativo RFC 5545 iCalendar (.ics)

### Integraciones Externas
- Proveedor de Inteligencia Artificial: xAI (Modelo Grok-2) via API REST
- Soporte de contingencia: Motor de analisis lexico-heuristico deterministico en backend

---

## Estructura del Repositorio

```
GoCampus/
|-- FRONT/
|   |-- doom-3d.js              # Motor de navegacion e interactividad 3D con Three.js
|   |-- index.html              # Estructura principal, planos SVG 2D, formularios y HUD
|   |-- script.js               # Logica de comunicacion REST, Web Components y calendario
|   `-- styles.css              # Sistema de diseno visual, animaciones y estilos responsivos
|
|-- backend/
|   |-- build.gradle            # Configuracion de dependencias y plugins de Gradle
|   |-- settings.gradle         # Identificador del proyecto backend (scgo)
|   |-- gradlew                 # Script ejecutable de Gradle para entornos Unix/Linux/macOS
|   |-- gradlew.bat             # Script ejecutable de Gradle para Windows
|   `-- src/
|       |-- main/
|       |   |-- java/com/campuslands/scgo/
|       |   |   |-- ScgoApplication.java          # Clase principal de arranque Spring Boot
|       |   |   |-- config/                       # Configuracion CORS y DataInitializer
|       |   |   |-- controller/                   # Controladores de la API REST
|       |   |   |-- dto/                          # Objetos de Transferencia de Datos (DTOs)
|       |   |   |-- exception/                    # Manejador global y excepciones de negocio
|       |   |   |-- model/                        # Entidades JPA del modelo relacional
|       |   |   |-- repository/                   # Interfaces Spring Data JPA
|       |   |   `-- service/                      # Interfaces e implementaciones de negocio
|       |   `-- resources/
|       |       |-- application.properties        # Configuracion principal (PostgreSQL + Grok)
|       |       `-- application-h2.properties     # Perfil liviano para desarrollo con H2
|       `-- test/
|           `-- java/com/campuslands/scgo/        # Pruebas unitarias de servicios de negocio
|
`-- README.md                   # Documentacion general del proyecto
```

---

## Reglas de Negocio Implementadas

El sistema valida formalmente las siguientes condiciones operativas mediante `BusinessException` y catalogo de codigos `ErrorCode`:

| Codigo de Error | Descripcion de la Regla |
| :--- | :--- |
| `OUTSIDE_OPERATING_HOURS` | Toda reserva debe crearse dentro de la franja valida institucional (06:00 - 23:00). |
| `OVERNIGHT_NOT_ALLOWED` | La hora final no puede ser menor o igual a la hora inicial cruzando medianoche. |
| `DURATION_OUT_OF_BOUNDS` | La duracion minima es de 15 minutos y la duracion maxima es de 3 horas continuas. |
| `CAPACITY_EXCEEDED` | El numero de asistentes registrados no puede sobrepasar el aforo maximo del espacio. |
| `OVERLAPPING_RESERVATION` | No pueden coexistir dos reservas en el mismo espacio con traslape horario parcial o total. |
| `SPACE_NOT_AVAILABLE` | El espacio fisico debe encontrarse en estado "disponible" (no en mantenimiento ni clausurado). |
| `RESERVATION_IN_PAST` | No se permite la planificacion ni creacion de reservas en fechas u horas pasadas. |
| `USER_INACTIVE_OR_SUSPENDED` | El usuario debe poseer estado "activo" en el sistema para cualquier tramite. |
| `ACTIVE_PENALTY_OVERDUE_LOAN` | Si el usuario tiene devoluciones atrasadas o sanciones activas, el prestamo se rechaza. |
| `LOAN_LIMIT_EXCEEDED` | Limite de un recurso en prestamo por categoria para un mismo usuario. |
| `RESOURCE_ALREADY_BORROWED` | El periferico seleccionado debe estar en inventario operativo y no asignado a otro usuario. |
| `INCIDENT_TARGET_REQUIRED` | Toda novedad tecnica debe asociarse a un espacio fisico o a un periferico especifico. |

---

## Requisitos Previos

Antes de ejecutar la aplicacion, verifique contar con las siguientes herramientas en su entorno:

- Java Development Kit (JDK): Version 21 o superior instalada y configurada en la variable `JAVA_HOME`.
- Git: Para la clonacion y administracion del codigo fuente.
- PostgreSQL (opcional para desarrollo local, requerido para produccion): Version 14 o superior.
- Navegador Web Moderno: Google Chrome, Mozilla Firefox, Microsoft Edge o Safari compatible con WebGL y soporte para JavaScript moderno.

---

## Instalacion y Configuracion

### 1. Clonacion del Repositorio

Abra una terminal y ejecute el siguiente comando:

```bash
git clone https://github.com/Alter-09/GoCampus.git
cd GoCampus
```

### 2. Configuracion del Backend

El backend se encuentra configurado para trabajar por defecto con PostgreSQL, pero cuenta con un perfil H2 en memoria listo para ejecucion inmediata sin dependencias externas de base de datos.

Archivo de configuracion principal:
`backend/src/main/resources/application.properties`

### 3. Variables de Entorno

Puede configurar las siguientes variables de entorno en su sistema operativo o archivo de configuracion:

| Variable | Descripcion | Valor por Defecto |
| :--- | :--- | :--- |
| `DB_URL` | URL de conexion JDBC para PostgreSQL | `jdbc:postgresql://localhost:5432/goCAMPUS` |
| `DB_USERNAME` | Usuario de base de datos | `admin` |
| `DB_PASSWORD` | Contrasena de base de datos | `admin123` |
| `CAMPUSLANDS_AI_KEY` | Llave de autenticacion para API de Grok / xAI | Clave preconfigurada del proyecto |
| `CAMPUSLANDS_AI_MODEL` | Modelo de lenguaje natural para triaje | `grok-2` |
| `CAMPUSLANDS_AI_BASE_URL` | Endpoint base de inferencia xAI | `https://api.x.ai/v1` |

### 4. Ejecucion del Backend

#### Opcion A: Ejecucion Rapida con Perfil H2 (Recomendada para revision y desarrollo)
No requiere tener PostgreSQL instalado. La base de datos se crea en memoria y se precargan los datos maestros automaticamente.

En Windows (PowerShell / CMD):
```cmd
cd backend
gradlew.bat bootRun --args="--spring.profiles.active=h2"
```

En Linux / macOS:
```bash
cd backend
chmod +x gradlew
./gradlew bootRun --args="--spring.profiles.active=h2"
```

Consola H2 disponible en: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:campuslands_db`
- User Name: `sa`
- Password: (vacio)

#### Opcion B: Ejecucion con PostgreSQL (Produccion)
Asegurese de que el servicio de PostgreSQL este activo y que la base de datos `goCAMPUS` este creada:

```sql
CREATE DATABASE "goCAMPUS";
```

Luego inicie el servidor:

En Windows:
```cmd
cd backend
gradlew.bat bootRun
```

En Linux / macOS:
```bash
cd backend
./gradlew bootRun
```

El servidor quedara escuchando peticiones en el puerto `http://localhost:8080`.

### 5. Ejecucion del Frontend

El cliente web es una SPA ligera construida en Vanilla JS sin dependencias de empaquetado complejas. Puede servirse a traves de cualquier servidor estatico local o extension de desarrollo.

#### Opcion con Servidor HTTP en Node.js:
Desde la raiz del proyecto o dentro del directorio `FRONT/`:
```bash
npx serve FRONT -p 5500
```

#### Opcion con Python 3:
```bash
cd FRONT
python -m http.server 5500
```

#### Opcion con Live Server (Visual Studio Code):
Haga clic derecho en `FRONT/index.html` y seleccione "Open with Live Server".

Abra en su navegador la URL correspondiente (por ejemplo: `http://localhost:5500`). La aplicacion se conectara automaticamente al backend en `http://localhost:8080/api`.

---

## Referencia de la API REST

Todos los endpoints responden en formato JSON y manejan cabeceras CORS permisivas para desarrollo local.

### Espacios Fisicos (`/api/espacios`)
- `GET /api/espacios`: Lista todos los espacios fisicos, sus tipos, ubicacion y aforos maximos.
- `GET /api/espacios/{id}`: Obtiene el detalle de un espacio por su identificador numerico.
- `POST /api/espacios`: Registra un nuevo espacio fisico en el sistema.
- `PUT /api/espacios/{id}`: Actualiza los datos generales de un espacio.
- `PATCH /api/espacios/{id}/coordenadas`: Permite al administrador reconfigurar las coordenadas X/Y en el plano.
- `PATCH /api/espacios/{id}/estado`: Modifica el estado operativo (disponible, mantenimiento, inhabilitado).
- `GET /api/espacios/sector/{sector}`: Filtra espacios segun el sector (Cabo Canaveral, ISS, Principal).

### Reservas de Espacios (`/api/reservas`)
- `POST /api/reservas`: Registra una nueva solicitud de reserva aplicando validaciones de aforo, horario y colision.
- `GET /api/reservas`: Lista todas las reservas registradas en el sistema.
- `GET /api/reservas/{id}`: Retorna una reserva por su identificador.
- `DELETE /api/reservas/{id}`: Cancela una reserva existente liberando el espacio en el calendario.
- `GET /api/reservas/usuario/{usuarioId}`: Consulta las reservas solicitadas por un usuario particular.
- `GET /api/reservas/espacio/{espacioId}`: Consulta el historico y reservas futuras de una sala o auditorio.
- `GET /api/reservas/calendario/usuario/{usuarioId}`: Retorna la vista semanal de eventos para el usuario.
- `GET /api/reservas/calendario/espacio/{espacioId}`: Retorna la vista semanal de eventos para la sala.
- `GET /api/reservas/minimapa?fechaHora=...`: Provee la informacion de ocupacion en tiempo real para pintar en verde o rojo los hotspots del mapa 2D y 3D.

### Recursos y Perifericos Tecnologicos (`/api/recursos`)
- `GET /api/recursos`: Retorna el inventario completo de periféricos y su disponibilidad.
- `GET /api/recursos/{id}`: Obtiene la ficha de un periferico especifico.
- `POST /api/recursos`: Registra un nuevo periferico tecnologico.
- `GET /api/recursos/categoria/{categoria}`: Filtra perifericos por tipo (mouse, teclado, cable_red, etc.).

### Prestamos de Hardware (`/api/prestamos`)
- `POST /api/prestamos`: Registra el prestamo de un periferico a un usuario previa comprobacion de sanciones y cupo.
- `POST /api/prestamos/{id}/devolucion`: Cierra el ciclo de prestamo registrando observaciones fisicas del retorno.
- `GET /api/prestamos/activos`: Lista los prestamos que se encuentran en posesion de los usuarios.
- `GET /api/prestamos/usuario/{usuarioId}`: Consulta los prestamos activos e historicos de un usuario.

### Incidencias y Triaje con IA (`/api/incidencias`)
- `POST /api/incidencias`: Envia un reporte de novedad tecnica, activa el triaje con Grok-2 (o fallback heuristico) y asigna prioridad y justificacion tecnica.
- `GET /api/incidencias`: Lista todas las novedades tecnicas registradas.
- `GET /api/incidencias/{id}`: Detalle de una incidencia y el diagnostico generado por la IA.
- `PATCH /api/incidencias/{id}/estado`: Permite a los agentes de soporte cambiar el estado de la incidencia (reportada, en_revision, resuelta, cancelada).
- `GET /api/incidencias/logs-ia`: Retorna la bitacora de auditoria del modelo de lenguaje (prompt enviado, tokens aproximados, latencia en milisegundos y nivel de confianza).

### Usuarios (`/api/usuarios`)
- `GET /api/usuarios`: Lista los usuarios y sus roles asignados (administrador, trainer, soporte, estudiante).
- `GET /api/usuarios/{id}`: Consulta el perfil y estado de un usuario.
- `POST /api/usuarios`: Da de alta un nuevo usuario en la base de datos.

---

## Ejecucion de Pruebas

El proyecto cuenta con un conjunto de pruebas unitarias automatizadas que cubren las reglas de negocio criticas (control de horarios, prevencion de colisiones y validacion de prestamos):

Para ejecutar la suite completa de pruebas:

En Windows:
```cmd
cd backend
gradlew.bat test
```

En Linux / macOS:
```bash
cd backend
./gradlew test
```

Los reportes HTML generados podran consultarse en la ruta:
`backend/build/reports/tests/test/index.html`

---

## Licencia

Este proyecto fue desarrollado con fines academicos e institucionales para Campuslands por el equipo de desarrollo GoCampus. Todos los derechos reservados.
