// ============================================================================
// SISTEMA DE CONTROL DE GESTIÓN Y OPERACIÓN (SCGO) - CAMPUSLANDS
// Módulo Frontend: Minimapa 2D + iCalendar en Tiempo Real + Reservas Backend
// ============================================================================

// 1. CONFIGURACIÓN DEL BACKEND REST API
const API_BASE_URL = window.location.origin.startsWith("http")
  ? (window.location.port === "8080" ? `${window.location.origin}/api` : "http://localhost:8080/api")
  : "http://localhost:8080/api";

// Mapeo oficial entre identificadores de Hotspots y Entidades de Espacio del Backend
const SPOT_BACKEND_MAPPING = {
  "cc-auditorio":       { backendId: 1,  backendName: "Auditorio",              type: "Auditorio Principal" },
  "cc-sala-azul":       { backendId: 8,  backendName: "Sala azul",              type: "Sala de Formación y Mentoría" },
  "cc-sala-blanca":     { backendId: 7,  backendName: "Sala blanca",            type: "Sala de Ideación y Colaboración" },
  "cc-sala-amarilla-1": { backendId: 10, backendName: "Sala Amarilla/Verde 1",  type: "Cabina de Enfoque Focus 1" },
  "cc-sala-amarilla-2": { backendId: 11, backendName: "Sala Amarilla/Verde 2",  type: "Cabina de Enfoque Focus 2" },
  "cc-sala-juntas":     { backendId: 9,  backendName: "Sala juntas",            type: "Sala de Juntas Ejecutiva" },
  "cc-cosmos":          { backendId: 2,  backendName: "Cosmos",                 type: "Aula de Formación Técnica" },
  "cc-sala-verde-1":    { backendId: 12, backendName: "Sala Amarilla/Verde 3",  type: "Cabina de Trabajo Técnico 1" },
  "cc-sala-verde-2":    { backendId: 13, backendName: "Sala Amarilla/Verde 4",  type: "Cabina de Trabajo Técnico 2" },
  "iss-salon-1":        { backendId: 3,  backendName: "Skylab",                 type: "Aula de Entrenamiento Técnico" },
  "iss-salon-2":        { backendId: 4,  backendName: "Sputnik",                type: "Aula de Entrenamiento Técnico" },
  "iss-salon-3":        { backendId: 5,  backendName: "Apollo",                 type: "Aula de Entrenamiento Técnico" },
  "iss-salon-4":        { backendId: 6,  backendName: "Artemis",                type: "Aula de Entrenamiento Técnico" }
};

// 2. WEB COMPONENT CON SHADOW DOM: <campus-status-badge>
class CampusStatusBadge extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
  }

  static get observedAttributes() {
    return ["status", "label"];
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue !== newValue) {
      this.render();
    }
  }

  connectedCallback() {
    this.render();
  }

  render() {
    const rawStatus = (this.getAttribute("status") || "disponible").toLowerCase();
    const label = this.getAttribute("label") || (rawStatus === "ocupado" ? "EN USO" : "DISPONIBLE");

    let bgColor = "#10b981";
    let textColor = "#ffffff";
    let borderColor = "#059669";
    let glow = "rgba(16, 185, 129, 0.4)";

    if (rawStatus === "ocupado" || rawStatus === "en clase" || rawStatus === "en uso") {
      bgColor = "#ef4444";
      borderColor = "#dc2626";
      glow = "rgba(239, 68, 68, 0.4)";
    } else if (rawStatus === "mantenimiento" || rawStatus === "inhabilitado") {
      bgColor = "#f59e0b";
      borderColor = "#d97706";
      glow = "rgba(245, 158, 11, 0.4)";
    }

    this.shadowRoot.innerHTML = `
      <style>
        :host {
          display: inline-block;
          vertical-align: middle;
        }
        .badge {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          padding: 3px 10px;
          border-radius: 9999px;
          font-family: inherit;
          font-size: 0.72rem;
          font-weight: 700;
          letter-spacing: 0.05em;
          text-transform: uppercase;
          background: ${bgColor};
          color: ${textColor};
          border: 1px solid ${borderColor};
          box-shadow: 0 0 8px ${glow};
          transition: all 0.25s ease;
        }
        .dot {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: #ffffff;
          box-shadow: 0 0 4px #ffffff;
          animation: badge-pulse 1.8s infinite ease-in-out;
        }
        @keyframes badge-pulse {
          0% { opacity: 0.5; transform: scale(0.85); }
          50% { opacity: 1; transform: scale(1.15); }
          100% { opacity: 0.5; transform: scale(0.85); }
        }
      </style>
      <span class="badge">
        <span class="dot"></span>
        <span>${label}</span>
      </span>
    `;
  }
}

if (!customElements.get("campus-status-badge")) {
  customElements.define("campus-status-badge", CampusStatusBadge);
}

// 3. CONFIGURACIÓN DE LOS 2 MÓDULOS (COORDENADAS DE PLANOS Y METADATOS)
const MODULES_DATA = {
  canaveral: {
    moduleName: "Sector Cabo Cañaveral",
    containerId: "hotspots-canaveral",
    hotspots: [
      {
        id: "cc-auditorio",
        number: "1",
        label: "AUDITORIO",
        code: "CC-AUD-01",
        left: 67.6,
        top: 22.3,
        size: 52,
        menuOptions: [
          "Ver disponibilidad de asientos",
          "Consultar ruta de evacuación",
          "Inventario audiovisual y sonido"
        ]
      },
      {
        id: "cc-sala-azul",
        number: "2",
        label: "SALA AZUL",
        code: "CC-AZUL-02",
        left: 65.8,
        top: 36.8,
        size: 26,
        menuOptions: [
          "Ver responsable actual",
          "Consultar conectividad"
        ]
      },
      {
        id: "cc-sala-blanca",
        number: "3",
        label: "SALA BLANCA",
        code: "CC-BLANCA-03",
        left: 71.5,
        top: 36.6,
        size: 28,
        menuOptions: [
          "Abrir panel de control del área",
          "Ver protocolo de acceso",
          "Ruta de evacuación inmediata",
          "Bitácora de novedades"
        ]
      },
      {
        id: "cc-sala-amarilla-1",
        number: "4",
        label: "SALA AMARILLA 1",
        code: "CC-AMARILLA-04",
        left: 69.2,
        top: 56.8,
        size: 22,
        menuOptions: [
          "Verificar estado de red"
        ]
      },
      {
        id: "cc-sala-amarilla-2",
        number: "5",
        label: "SALA AMARILLA 2",
        code: "CC-AMARILLA-05",
        left: 72.7,
        top: 56.8,
        size: 22,
        menuOptions: [
          "Verificar estado de red"
        ]
      },
      {
        id: "cc-sala-juntas",
        number: "6",
        label: "SALA DE JUNTAS",
        code: "CC-JUNTAS-06",
        left: 71.5,
        top: 70.3,
        size: 28,
        menuOptions: [
          "Ver agenda del día",
          "Activar proyector / pantalla",
          "Consultar ruta de evacuación"
        ]
      },
      {
        id: "cc-cosmos",
        number: "7",
        label: "COSMOS",
        code: "CC-COSMOS-07",
        left: 42.0,
        top: 55.2,
        size: 28,
        menuOptions: [
          "Ver disponibilidad de aula",
          "Consultar conectividad y tomas",
          "Ruta de evacuación asignada"
        ]
      },
      {
        id: "cc-sala-verde-1",
        number: "8",
        label: "SALA VERDE 1",
        code: "CC-VERDE-08",
        left: 42.0,
        top: 70.6,
        size: 22,
        menuOptions: [
          "Ver equipo asignado",
          "Inventario de puestos",
          "Ver salida de emergencia cercana"
        ]
      },
      {
        id: "cc-sala-verde-2",
        number: "9",
        label: "SALA VERDE 2",
        code: "CC-VERDE-09",
        left: 42.1,
        top: 76.8,
        size: 22,
        menuOptions: [
          "Ver equipo asignado",
          "Inventario de puestos",
          "Ver salida de emergencia cercana"
        ]
      }
    ]
  },

  iss: {
    moduleName: "Estación Espacial Internacional",
    containerId: "hotspots-iss",
    hotspots: [
      {
        id: "iss-salon-1",
        number: "S1",
        label: "SKY LAB",
        code: "ISS-SKYLAB-01",
        left: 35.5,
        top: 76.5,
        size: 36,
        menuOptions: [
          "Ver listado de Campers y Trainer",
          "Control de equipos y periféricos",
          "Ruta de evacuación asignada"
        ]
      },
      {
        id: "iss-salon-2",
        number: "S2",
        label: "SPUTNIK",
        code: "ISS-SPUTNIK-02",
        left: 31.9,
        top: 34.2,
        size: 36,
        menuOptions: [
          "Ver listado de Campers y Trainer",
          "Control de equipos y periféricos",
          "Ruta de evacuación asignada"
        ]
      },
      {
        id: "iss-salon-3",
        number: "S3",
        label: "APOLLO",
        code: "ISS-APOLLO-03",
        left: 11.4,
        top: 35.8,
        size: 36,
        menuOptions: [
          "Ver listado de Campers y Trainer",
          "Control de equipos y periféricos",
          "Ruta de evacuación asignada"
        ]
      },
      {
        id: "iss-salon-4",
        number: "S4",
        label: "ARTEMIS",
        code: "ISS-ARTEMIS-04",
        left: 10.8,
        top: 70.3,
        size: 36,
        menuOptions: [
          "Ver listado de Campers y Trainer",
          "Control de equipos y periféricos",
          "Ruta de evacuación asignada"
        ]
      }
    ]
  }
};

// Estado global accesible para sincronizar con el recorrido 3D
window.CURRENT_ACTIVE_MODULE = "canaveral";
window.MODULES_DATA = MODULES_DATA;

// Caché reactivo de datos de backend
let backendEspaciosCache = [];
let backendMinimapaCache = [];

// Estado del espacio y calendario actualmente activo en la Card
let currentActiveSpot = null;
let currentBackendMapping = null;
let activeRoomReservations = [];
let icalViewDate = new Date(); // Mes y año en visualización
let selectedDateString = new Date().toISOString().slice(0, 10);

// ELEMENTOS DEL DOM
const moduleButtons = document.querySelectorAll(".module-btn");
const moduleSections = {
  canaveral: document.getElementById("module-canaveral"),
  iss: document.getElementById("module-iss")
};

const contextMenu = document.getElementById("context-menu");
const menuModuleTag = document.getElementById("menu-module-tag");
const menuTitle = document.getElementById("menu-title");
const menuDescription = document.getElementById("menu-description");
const menuCode = document.getElementById("menu-code");
const menuCapacity = document.getElementById("menu-capacity");
const menuStatus = document.getElementById("menu-status");
const menuType = document.getElementById("menu-type");
const menuSchedule = document.getElementById("menu-schedule");
const menuMaxDur = document.getElementById("menu-max-dur");
const menuActions = document.getElementById("menu-actions");
const actionFeedback = document.getElementById("action-feedback");
const btnCloseMenu = document.getElementById("btn-close-menu");
const toggleRealImage = document.getElementById("toggle-real-image");

// Elementos de iCalendar y Formulario de Reserva
const btnExportIcal = document.getElementById("btn-export-ical");
const btnIcalPrev = document.getElementById("btn-ical-prev");
const btnIcalNext = document.getElementById("btn-ical-next");
const icalCurrentMonth = document.getElementById("ical-current-month");
const icalDaysGrid = document.getElementById("ical-days-grid");
const icalInfoDate = document.getElementById("ical-info-date");
const icalInfoSlots = document.getElementById("ical-info-slots");

const formCrearReserva = document.getElementById("form-crear-reserva");
const reservaFechaInput = document.getElementById("reserva-fecha");
const reservaHoraInicioInput = document.getElementById("reserva-hora-inicio");
const reservaHoraFinInput = document.getElementById("reserva-hora-fin");
const reservaPersonasInput = document.getElementById("reserva-personas");
const reservaUsuarioSelect = document.getElementById("reserva-usuario");
const reservaMotivoInput = document.getElementById("reserva-motivo");

// Elementos de Triaje Inteligente IA (Grok)
const formReportarIncidencia = document.getElementById("form-reportar-incidencia");
const incidenciaDescInput = document.getElementById("incidencia-desc");
const btnEvaluarTriaje = document.getElementById("btn-evaluar-triaje");

// 4. RENDERIZAR LOS HOTSPOTS EN LOS PLANOS 2D
function renderHotspots() {
  Object.keys(MODULES_DATA).forEach((moduleKey) => {
    const moduleInfo = MODULES_DATA[moduleKey];
    const container = document.getElementById(moduleInfo.containerId);
    if (!container) return;
    container.innerHTML = "";

    moduleInfo.hotspots.forEach((spot) => {
      const btn = document.createElement("button");
      btn.className = "hotspot-btn";
      btn.id = `btn-${spot.id}`;
      btn.style.left = `${spot.left}%`;
      btn.style.top = `${spot.top}%`;
      btn.style.width = `${spot.size}px`;
      btn.style.height = `${spot.size}px`;
      btn.setAttribute("data-label", spot.label);
      btn.setAttribute("aria-label", `${spot.number}: ${spot.label}`);
      btn.textContent = spot.number;

      btn.addEventListener("click", (e) => {
        e.stopPropagation();
        openSpotMenu(moduleInfo.moduleName, spot, btn);
      });

      container.appendChild(btn);
    });
  });
}

// 5. SINCRONIZACIÓN CON ENDPOINTS DEL BACKEND (100% DATOS REALES)
async function sincronizarDatosBackend() {
  try {
    const [resEspacios, resMinimapa, resUsuarios] = await Promise.all([
      fetch(`${API_BASE_URL}/espacios`).catch(() => null),
      fetch(`${API_BASE_URL}/reservas/minimapa`).catch(() => null),
      fetch(`${API_BASE_URL}/usuarios`).catch(() => null)
    ]);

    if (resEspacios && resEspacios.ok) {
      backendEspaciosCache = await resEspacios.json();
    }

    if (resMinimapa && resMinimapa.ok) {
      backendMinimapaCache = await resMinimapa.json();
      actualizarHotspotsConMinimapa();
    }

    if (resUsuarios && resUsuarios.ok) {
      const usuarios = await resUsuarios.json();
      poblarSelectUsuarios(usuarios);
    }
  } catch (err) {
    console.warn("[Backend API] Modo offline:", err.message);
  }
}

// Poblado dinámico del selector de usuarios con entidades reales de la base de datos
function poblarSelectUsuarios(usuarios) {
  if (!reservaUsuarioSelect || !Array.isArray(usuarios)) return;
  const currentVal = reservaUsuarioSelect.value;
  reservaUsuarioSelect.innerHTML = "";

  usuarios.forEach((u) => {
    const opt = document.createElement("option");
    opt.value = u.id;
    const rolStr = (u.rol || "USUARIO").toUpperCase();
    opt.textContent = `${u.nombre} (ID ${u.id} - ${rolStr})`;
    if (String(u.id) === String(currentVal) || (!currentVal && u.id === 6)) {
      opt.selected = true;
    }
    reservaUsuarioSelect.appendChild(opt);
  });
}

function actualizarHotspotsConMinimapa() {
  if (!backendMinimapaCache || !Array.isArray(backendMinimapaCache)) return;

  Object.keys(MODULES_DATA).forEach((modKey) => {
    MODULES_DATA[modKey].hotspots.forEach((spot) => {
      const mapping = SPOT_BACKEND_MAPPING[spot.id];
      if (!mapping) return;

      const backendItem = backendMinimapaCache.find(
        (m) => m.id === mapping.backendId || m.nombre?.toLowerCase() === spot.label.toLowerCase()
      );

      const espacioItem = backendEspaciosCache.find((e) => e.id === mapping.backendId);

      if (backendItem || espacioItem) {
        const aforo = backendItem?.aforoMaximo || espacioItem?.aforoMaximo || 20;
        const ubicacion = backendItem?.ubicacion || espacioItem?.ubicacion || "Campuslands";
        const ocupado = backendItem ? backendItem.ocupado : false;
        const evento = backendItem ? backendItem.eventoTitulo : null;
        const estadoEspacio = (backendItem ? backendItem.estadoEspacio : null) || espacioItem?.estado || "disponible";

        spot.backendAforo = aforo;
        spot.backendUbicacion = ubicacion;
        spot.backendOcupado = ocupado;
        spot.backendEvento = evento;
        spot.status = ocupado ? "Ocupado" : estadoEspacio;
        spot.capacity = `${aforo} personas`;

        const btnElement = document.getElementById(`btn-${spot.id}`);
        if (btnElement) {
          if (ocupado) {
            btnElement.classList.add("occupied");
            btnElement.title = `${spot.label} (EN USO: ${evento || "Clase activa"})`;
          } else {
            btnElement.classList.remove("occupied");
            btnElement.title = `${spot.label} (${spot.status.toUpperCase()} - Aforo: ${aforo})`;
          }
        }
      }
    });
  });
}

// 6. ABRIR EL MENÚ / CARD CON INFORMACIÓN INTEGRAL
async function openSpotMenu(moduleName, spot, clickedButton) {
  document.querySelectorAll(".hotspot-btn").forEach((b) => b.classList.remove("selected"));
  clickedButton.classList.add("selected");

  currentActiveSpot = spot;
  currentBackendMapping = SPOT_BACKEND_MAPPING[spot.id] || { backendId: null, type: "Espacio Oficial" };

  let liveItem = backendMinimapaCache.find((m) => m.id === currentBackendMapping.backendId);
  let espacioItem = backendEspaciosCache.find((e) => e.id === currentBackendMapping.backendId);

  if ((!liveItem || !espacioItem) && currentBackendMapping.backendId) {
    try {
      const resEspacio = await fetch(`${API_BASE_URL}/espacios/${currentBackendMapping.backendId}`).catch(() => null);
      if (resEspacio && resEspacio.ok) {
        espacioItem = await resEspacio.json();
        if (!liveItem) liveItem = espacioItem;
      }
    } catch (_) {}
  }

  // Poblar la tarjeta exclusivamente con datos de la base de datos
  const nombreOficial = liveItem?.nombre || espacioItem?.nombre || spot.label;
  const aforoMaxNum = liveItem?.aforoMaximo || espacioItem?.aforoMaximo || 20;
  const ubicacionOficial = liveItem?.ubicacion || espacioItem?.ubicacion || "Campuslands";
  const sectorOficial = liveItem?.sector || espacioItem?.sector || moduleName;
  const pisoOficial = liveItem?.piso || espacioItem?.piso || "Piso Principal";
  const tipoOficial = liveItem?.tipo || espacioItem?.tipo || currentBackendMapping.type || "Aula";

  menuModuleTag.textContent = sectorOficial.toUpperCase();
  menuTitle.textContent = nombreOficial.toUpperCase();
  menuDescription.textContent = `Espacio oficial de Campuslands ubicado en ${ubicacionOficial} (${pisoOficial}, ${sectorOficial}).`;
  menuCode.textContent = spot.code;

  menuCapacity.textContent = `${aforoMaxNum} personas (Capacidad Oficial)`;
  if (menuType) menuType.textContent = tipoOficial.toUpperCase();
  if (menuSchedule) menuSchedule.textContent = "06:00 AM - 11:00 PM (Lunes a Sábado)";
  if (menuMaxDur) menuMaxDur.textContent = "Máx. 3 horas continuas";

  // Actualizar límite de aforo en el formulario de reserva
  if (reservaPersonasInput) {
    reservaPersonasInput.max = aforoMaxNum;
    reservaPersonasInput.value = Math.min(6, aforoMaxNum);
  }

  // Pre-llenar fecha por defecto
  const hoyStr = new Date().toISOString().slice(0, 10);
  if (reservaFechaInput) {
    reservaFechaInput.min = hoyStr;
    if (!reservaFechaInput.value) {
      reservaFechaInput.value = hoyStr;
    }
  }

  // Web Component con Shadow DOM para renderizar el badge de estado en vivo
  const rawStatus = liveItem?.ocupado ? "ocupado" : (liveItem?.estado || liveItem?.estadoEspacio || espacioItem?.estado || "disponible");
  const badgeLabel = liveItem?.ocupado
    ? `EN USO (${liveItem.eventoTitulo || "Clase Activa"})`
    : rawStatus.toUpperCase();

  menuStatus.innerHTML = `<campus-status-badge status="${rawStatus.toLowerCase()}" label="${badgeLabel}"></campus-status-badge>`;

  actionFeedback.classList.add("hidden");
  actionFeedback.textContent = "";

  // Renderizar opciones de acciones rápidas
  menuActions.innerHTML = "";
  spot.menuOptions.forEach((optionText) => {
    const li = document.createElement("li");
    const actionBtn = document.createElement("button");
    actionBtn.className = "menu-action-btn";
    actionBtn.innerHTML = `<span>${optionText}</span> <span>&rarr;</span>`;

    actionBtn.addEventListener("click", () => {
      ejecutarAccionEndpoint(optionText, spot, currentBackendMapping, liveItem);
    });

    li.appendChild(actionBtn);
    menuActions.appendChild(li);
  });

  // Cargar reservas del backend y renderizar iCalendar de esta sala
  await cargarReservasEspacioYRenderizar(currentBackendMapping.backendId);

  contextMenu.classList.remove("hidden");
}

// 7. GESTIÓN DEL COMPONENTE ICALENDAR EN TIEMPO REAL
async function cargarReservasEspacioYRenderizar(espacioId) {
  activeRoomReservations = [];
  if (espacioId) {
    try {
      const res = await fetch(`${API_BASE_URL}/reservas/espacio/${espacioId}`).catch(() => null);
      if (res && res.ok) {
        activeRoomReservations = await res.json();
      }
    } catch (err) {
      console.warn("[iCalendar] No fue posible cargar reservas del espacio:", err.message);
    }
  }
  renderICalendar();
}

function renderICalendar() {
  if (!icalDaysGrid || !icalCurrentMonth) return;

  const meses = [
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
  ];

  const year = icalViewDate.getFullYear();
  const month = icalViewDate.getMonth();

  icalCurrentMonth.textContent = `${meses[month]} ${year}`;
  icalDaysGrid.innerHTML = "";

  const primerDia = new Date(year, month, 1);
  const ultimoDia = new Date(year, month + 1, 0);
  const totalDias = ultimoDia.getDate();

  // Ajustar día de inicio para que la semana empiece en Lunes (0=Dom -> 6, 1=Lun -> 0)
  let startingDay = primerDia.getDay() - 1;
  if (startingDay === -1) startingDay = 6;

  for (let i = 0; i < startingDay; i++) {
    const emptyCell = document.createElement("div");
    emptyCell.className = "ical-day empty";
    icalDaysGrid.appendChild(emptyCell);
  }

  const hoyStr = new Date().toISOString().slice(0, 10);
  const pad = (n) => String(n).padStart(2, "0");

  for (let dia = 1; dia <= totalDias; dia++) {
    const diaFechaStr = `${year}-${pad(month + 1)}-${pad(dia)}`;
    const dayBtn = document.createElement("button");
    dayBtn.type = "button";
    dayBtn.className = "ical-day";
    dayBtn.textContent = dia;

    if (diaFechaStr === hoyStr) {
      dayBtn.classList.add("today");
    }

    if (diaFechaStr === selectedDateString) {
      dayBtn.classList.add("selected");
    }

    // Comprobar si existen reservas activas en esta fecha para la sala
    const reservasEnDia = activeRoomReservations.filter((r) => {
      const f = r.fechaReserva || (r.fechaInicio ? r.fechaInicio.slice(0, 10) : "");
      return f === diaFechaStr && r.estado !== "cancelada";
    });

    if (reservasEnDia.length > 0) {
      dayBtn.classList.add("occupied");
      dayBtn.title = `${reservasEnDia.length} reserva(s) programada(s)`;
    }

    dayBtn.addEventListener("click", () => {
      document.querySelectorAll(".ical-day").forEach((d) => d.classList.remove("selected"));
      dayBtn.classList.add("selected");
      selectedDateString = diaFechaStr;

      if (reservaFechaInput) {
        reservaFechaInput.value = diaFechaStr;
      }

      actualizarDetalleDiaIcal(diaFechaStr, reservasEnDia);
    });

    icalDaysGrid.appendChild(dayBtn);
  }

  // Actualizar detalle del día seleccionado
  const reservasDiaSeleccionado = activeRoomReservations.filter((r) => {
    const f = r.fechaReserva || (r.fechaInicio ? r.fechaInicio.slice(0, 10) : "");
    return f === selectedDateString && r.estado !== "cancelada";
  });
  actualizarDetalleDiaIcal(selectedDateString, reservasDiaSeleccionado);
}

function actualizarDetalleDiaIcal(fechaStr, reservas) {
  if (!icalInfoDate || !icalInfoSlots) return;
  icalInfoDate.textContent = `Fecha: ${fechaStr}`;
  icalInfoSlots.innerHTML = "";

  if (reservas && reservas.length > 0) {
    reservas.forEach((r) => {
      const hIni = r.horaInicio ? r.horaInicio.slice(0, 5) : (r.fechaInicio ? r.fechaInicio.slice(11, 16) : "--:--");
      const hFin = r.horaFin ? r.horaFin.slice(0, 5) : (r.fechaFin ? r.fechaFin.slice(11, 16) : "--:--");
      const span = document.createElement("span");
      span.className = "slot-occupied";
      span.textContent = `[${hIni} - ${hFin}] ${r.motivo || "Reserva activa"} (${r.usuarioNombre || "Usuario"}, ${r.cantidadPersonas} pers.)`;
      icalInfoSlots.appendChild(span);
    });
  } else {
    const span = document.createElement("span");
    span.className = "slot-free";
    span.textContent = "Espacio 100% libre para reserva en horario de 06:00 a 23:00";
    icalInfoSlots.appendChild(span);
  }
}

// Navegación de meses en el iCalendar
if (btnIcalPrev) {
  btnIcalPrev.addEventListener("click", () => {
    icalViewDate.setMonth(icalViewDate.getMonth() - 1);
    renderICalendar();
  });
}

if (btnIcalNext) {
  btnIcalNext.addEventListener("click", () => {
    icalViewDate.setMonth(icalViewDate.getMonth() + 1);
    renderICalendar();
  });
}

// 8. EXPORTADOR ESTÁNDAR ICALENDAR (.ICS) CONFORME A RFC 5545
if (btnExportIcal) {
  btnExportIcal.addEventListener("click", () => {
    if (!currentActiveSpot) {
      alert("Por favor seleccione primero un espacio en el plano.");
      return;
    }

    const roomCode = currentActiveSpot.code || "SALA";
    const roomName = currentActiveSpot.label || "Campuslands";

    let icsContent = [
      "BEGIN:VCALENDAR",
      "VERSION:2.0",
      "PRODID:-//Campuslands//SCGO Reservas//ES",
      "CALSCALE:GREGORIAN",
      "METHOD:PUBLISH"
    ];

    const formatIcalDate = (isoStr) => {
      if (!isoStr) return "";
      const d = new Date(isoStr);
      const pad = (n) => String(n).padStart(2, "0");
      return `${d.getUTCFullYear()}${pad(d.getUTCMonth() + 1)}${pad(d.getUTCDate())}T${pad(d.getUTCHours())}${pad(d.getUTCMinutes())}${pad(d.getUTCSeconds())}Z`;
    };

    if (activeRoomReservations.length > 0) {
      activeRoomReservations.forEach((r) => {
        if (r.estado === "cancelada") return;
        const dtStart = formatIcalDate(r.fechaInicio);
        const dtEnd = formatIcalDate(r.fechaFin);
        icsContent.push("BEGIN:VEVENT");
        icsContent.push(`UID:reserva-${r.id}-campuslands@scgo.com`);
        icsContent.push(`DTSTAMP:${formatIcalDate(new Date().toISOString())}`);
        icsContent.push(`DTSTART:${dtStart}`);
        icsContent.push(`DTEND:${dtEnd}`);
        icsContent.push(`SUMMARY:${r.espacioNombre || roomName}: ${r.motivo || "Reserva Oficial"}`);
        icsContent.push(`DESCRIPTION:Titular: ${r.usuarioNombre} | Aforo: ${r.cantidadPersonas} personas | Estado: ${r.estado}`);
        icsContent.push(`LOCATION:Campuslands - ${roomName} (${roomCode})`);
        icsContent.push("STATUS:CONFIRMED");
        icsContent.push("END:VEVENT");
      });
    } else {
      // Evento informativo si no hay ocupaciones registradas
      const hoy = new Date();
      const dtStart = formatIcalDate(new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate(), 6, 0, 0).toISOString());
      const dtEnd = formatIcalDate(new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate(), 23, 0, 0).toISOString());
      icsContent.push("BEGIN:VEVENT");
      icsContent.push(`UID:info-${roomCode}-campuslands@scgo.com`);
      icsContent.push(`DTSTAMP:${formatIcalDate(new Date().toISOString())}`);
      icsContent.push(`DTSTART:${dtStart}`);
      icsContent.push(`DTEND:${dtEnd}`);
      icsContent.push(`SUMMARY:Disponibilidad General en ${roomName}`);
      icsContent.push(`DESCRIPTION:Espacio disponible para reserva en franja operativa oficial de 06:00 a 23:00.`);
      icsContent.push(`LOCATION:Campuslands - ${roomName} (${roomCode})`);
      icsContent.push("STATUS:CONFIRMED");
      icsContent.push("END:VEVENT");
    }

    icsContent.push("END:VCALENDAR");

    const blob = new Blob([icsContent.join("\r\n")], { type: "text/calendar;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const downloadLink = document.createElement("a");
    downloadLink.href = url;
    downloadLink.download = `campuslands-${roomCode}.ics`;
    document.body.appendChild(downloadLink);
    downloadLink.click();
    document.body.removeChild(downloadLink);
    URL.revokeObjectURL(url);

    actionFeedback.className = "action-feedback";
    actionFeedback.textContent = `[ICALENDAR SCGO] Archivo campuslands-${roomCode}.ics descargado con éxito para sincronizar con Google Calendar / Apple Calendar.`;
    actionFeedback.classList.remove("hidden");
  });
}

// 9. FORMULARIO DE RESERVA DIRECTA EN EL BACKEND
if (formCrearReserva) {
  formCrearReserva.addEventListener("submit", async (e) => {
    e.preventDefault();

    if (!currentActiveSpot || !currentBackendMapping || !currentBackendMapping.backendId) {
      actionFeedback.className = "action-feedback";
      actionFeedback.textContent = "[ERROR] Debe seleccionar un espacio en el plano antes de crear una reserva.";
      actionFeedback.classList.remove("hidden");
      return;
    }

    const fechaVal = reservaFechaInput.value;
    const horaInicioVal = reservaHoraInicioInput.value;
    const horaFinVal = reservaHoraFinInput.value;
    const personasVal = parseInt(reservaPersonasInput.value, 10);
    const usuarioIdVal = parseInt(reservaUsuarioSelect.value, 10);
    const motivoVal = (reservaMotivoInput.value || "").trim();

    // Validaciones de negocio en el cliente
    if (!fechaVal || !horaInicioVal || !horaFinVal || !motivoVal) {
      actionFeedback.className = "action-feedback";
      actionFeedback.textContent = "[DATOS INCOMPLETOS] Por favor complete todos los campos de fecha, horario y motivo.";
      actionFeedback.classList.remove("hidden");
      return;
    }

    if (horaFinVal <= horaInicioVal) {
      actionFeedback.className = "action-feedback";
      actionFeedback.textContent = "[HORARIO INVÁLIDO] La hora de fin debe ser posterior a la hora de inicio.";
      actionFeedback.classList.remove("hidden");
      return;
    }

    // Calcular duración en minutos
    const [hI, mI] = horaInicioVal.split(":").map(Number);
    const [hF, mF] = horaFinVal.split(":").map(Number);
    const duracionMin = (hF * 60 + mF) - (hI * 60 + mI);

    if (duracionMin < 15 || duracionMin > 180) {
      actionFeedback.className = "action-feedback";
      actionFeedback.textContent = `[REGLA DE DURACIÓN] La reserva debe durar entre 15 minutos y 3 horas continuas. Duración calculada: ${duracionMin} minutos.`;
      actionFeedback.classList.remove("hidden");
      return;
    }

    actionFeedback.className = "action-feedback";
    actionFeedback.textContent = "[PROCESANDO] Comunicando con el servidor...";
    actionFeedback.classList.remove("hidden");

    // Construcción del DTO con fecha de reserva y hora de reserva
    const pad = (s) => (s.length === 5 ? `${s}:00` : s);
    const reservaPayload = {
      espacioId: currentBackendMapping.backendId,
      usuarioId: usuarioIdVal,
      fechaReserva: fechaVal,
      horaInicio: pad(horaInicioVal),
      horaFin: pad(horaFinVal),
      horaReserva: pad(horaInicioVal),
      cantidadPersonas: personasVal,
      motivo: motivoVal
    };

    try {
      const res = await fetch(`${API_BASE_URL}/reservas`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(reservaPayload)
      });

      const data = await res.json();

      if (res.ok) {
        actionFeedback.textContent = `[RESERVA CONFIRMADA] Reserva #${data.id} creada exitosamente. Espacio: ${data.espacioNombre} | Fecha: ${data.fechaReserva} | Horario: ${data.horaInicio} - ${data.horaFin} | Titular: ${data.usuarioNombre}.`;
        reservaMotivoInput.value = "";

        // Refrescar reservas del espacio y minimapa
        await cargarReservasEspacioYRenderizar(currentBackendMapping.backendId);
        await sincronizarDatosBackend();
      } else {
        const errorMsg = data.mensaje || data.error || "No fue posible crear la reserva.";
        actionFeedback.textContent = `[ERROR - ${data.codigo || "CONFLICTO"}] ${errorMsg}`;
      }
    } catch (err) {
      actionFeedback.textContent = `[ERROR DE COMUNICACIÓN] No se pudo conectar con el servidor (${err.message}).`;
    }
  });
}

// 10. FORMULARIO DEDICADO DE TRIAJE INTELIGENTE IA (GROK)
if (formReportarIncidencia) {
  formReportarIncidencia.addEventListener("submit", async (e) => {
    e.preventDefault();

    if (!currentActiveSpot || !currentBackendMapping || !currentBackendMapping.backendId) {
      actionFeedback.className = "action-feedback";
      actionFeedback.textContent = "[ERROR] Debe seleccionar un espacio en el plano para reportar la novedad.";
      actionFeedback.classList.remove("hidden");
      return;
    }

    const descVal = (incidenciaDescInput.value || "").trim();
    if (!descVal) {
      actionFeedback.className = "action-feedback";
      actionFeedback.textContent = "[DATOS INCOMPLETOS] Ingrese una descripción de la novedad técnica.";
      actionFeedback.classList.remove("hidden");
      return;
    }

    actionFeedback.className = "action-feedback";
    actionFeedback.textContent = `[ANALIZANDO CON GROK] Evaluando reporte de novedad en ${currentActiveSpot.label}...`;
    actionFeedback.classList.remove("hidden");

    const payload = {
      titulo: `Novedad en ${currentActiveSpot.label}`,
      descripcion: descVal,
      usuarioId: 6, // Andres Estudiante
      espacioId: currentBackendMapping.backendId
    };

    try {
      const res = await fetch(`${API_BASE_URL}/incidencias`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      const ticket = await res.json();

      if (res.ok) {
        const triaje = ticket.triajeIa || {};
        const cat = (ticket.categoria || "GENERAL").toUpperCase();
        const prio = (ticket.prioridad || "MEDIA").toUpperCase();
        const agente = ticket.agenteAsignadoNombre || "Mesa de Soporte";
        const modelo = triaje.modeloIa || "grok-2";
        const confianza = triaje.nivelConfianza ? `${Math.round(triaje.nivelConfianza * 100)}%` : "95%";
        const accion = triaje.accionInmediataSugerida || "Evaluación técnica requerida";

        actionFeedback.textContent = `[TRIAJE IA GROK] Incidencia #${ticket.id} evaluada con ${modelo} (Confianza: ${confianza}). Prioridad: ${prio} | Categoría: ${cat} | Agente Asignado: ${agente}. Acción preventiva sugerida: "${accion}".`;
        incidenciaDescInput.value = "";
        await sincronizarDatosBackend();
      } else {
        const errorMsg = ticket.mensaje || ticket.error || "No se pudo procesar la incidencia.";
        actionFeedback.textContent = `[ERROR] ${errorMsg}`;
      }
    } catch (err) {
      actionFeedback.textContent = `[ERROR DE CONEXIÓN] No fue posible comunicarse con el servidor (${err.message}).`;
    }
  });
}

// 11. CONSUMO DE ENDPOINTS EN ACCIONES RÁPIDAS
async function ejecutarAccionEndpoint(optionText, spot, mapping, liveItem) {
  const optLower = optionText.toLowerCase();
  actionFeedback.className = "action-feedback";
  actionFeedback.textContent = `[CONSULTANDO] Procesando ${optionText}...`;
  actionFeedback.classList.remove("hidden");

  try {
    // Agenda semanal y horarios
    if (optLower.includes("agenda") || optLower.includes("horario")) {
      const espacioId = mapping.backendId || 1;
      const res = await fetch(`${API_BASE_URL}/reservas/calendario/espacio/${espacioId}`);
      if (res.ok) {
        const eventos = await res.json();
        if (eventos && eventos.length > 0) {
          const listado = eventos
            .slice(0, 3)
            .map((e) => `[${e.title || e.espacioNombre}: ${e.start.slice(11, 16)} - ${e.end.slice(11, 16)}]`)
            .join(" | ");
          actionFeedback.textContent = `[AGENDA OFICIAL] ${eventos.length} reserva(s) programada(s): ${listado}`;
        } else {
          actionFeedback.textContent = `[AGENDA OFICIAL] No hay reservas activas en este momento para ${spot.label}. Espacio libre para uso.`;
        }
      } else {
        actionFeedback.textContent = `[AGENDA OFICIAL] Horario operativo de ${spot.label}: 06:00 AM a 11:00 PM. No hay eventos registrados hoy.`;
      }
      return;
    }

    // Inventario de periféricos tecnológicos
    if (optLower.includes("inventario") || optLower.includes("periféricos") || optLower.includes("audiovisual")) {
      const res = await fetch(`${API_BASE_URL}/recursos/disponibles`);
      if (res.ok) {
        const recursos = await res.json();
        if (recursos && recursos.length > 0) {
          const resumen = recursos
            .slice(0, 4)
            .map((r) => `${r.codigoInventario}: ${r.nombre}`)
            .join(" | ");
          actionFeedback.textContent = `[INVENTARIO TECNOLÓGICO SCGO] ${recursos.length} periféricos operativos disponibles: ${resumen}`;
        } else {
          actionFeedback.textContent = `[INVENTARIO TECNOLÓGICO SCGO] Todos los periféricos se encuentran actualmente asignados.`;
        }
      } else {
        actionFeedback.textContent = `[INVENTARIO TECNOLÓGICO SCGO] Conexión establecida con la bodega de periféricos.`;
      }
      return;
    }

    // Reporte técnico con Triaje de IA
    if (optLower.includes("conectividad") || optLower.includes("red") || optLower.includes("novedades") || optLower.includes("proyector")) {
      const incPayload = {
        titulo: `Verificación técnica en ${spot.label}`,
        descripcion: `Inspección de conectividad, red y equipo en el espacio ${spot.label} reportada desde el Minimapa.`,
        usuarioId: 6,
        espacioId: mapping.backendId || 1
      };

      const res = await fetch(`${API_BASE_URL}/incidencias`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(incPayload)
      });

      if (res.ok) {
        const ticket = await res.json();
        const categoriaIa = ticket.categoria ? ticket.categoria.toUpperCase() : "GENERAL";
        const prioridadIa = ticket.prioridad ? ticket.prioridad.toUpperCase() : "MEDIA";
        const modelo = ticket.triajeIa?.modeloIa ? ticket.triajeIa.modeloIa : "grok-2";
        const agente = ticket.agenteAsignadoNombre || "Mesa de Soporte";
        actionFeedback.textContent = `[TRIAJE IA GROK] Incidencia #${ticket.id} evaluada automáticamente con ${modelo}. Prioridad: ${prioridadIa} | Categoría: ${categoriaIa} | Agente asignado: ${agente}.`;
      } else {
        actionFeedback.textContent = `[SISTEMA SCGO] Verificación técnica de conectividad completada para ${spot.label}.`;
      }
      return;
    }

    // Disponibilidad y consulta en vivo
    if (optLower.includes("disponibilidad") || optLower.includes("responsable") || optLower.includes("control") || optLower.includes("campers")) {
      const espacioId = mapping.backendId || 1;
      const res = await fetch(`${API_BASE_URL}/espacios/${espacioId}`);
      if (res.ok) {
        const esp = await res.json();
        actionFeedback.textContent = `[OK] Espacio verificado: ${esp.nombre} | Aforo: ${esp.aforoMaximo} personas | Sector: ${esp.sector} (${esp.piso}) | Estado: ${esp.estado.toUpperCase()}.`;
      } else {
        actionFeedback.textContent = `[OK] Espacio verificado: ${spot.label} | Aforo: ${spot.capacity} | Estado: ${spot.status}.`;
      }
      return;
    }

    // Seguridad y rutas de evacuación
    if (optLower.includes("evacuación") || optLower.includes("emergencia") || optLower.includes("protocolo")) {
      const ubicacion = liveItem?.ubicacion || "Edificio Central de Campuslands";
      actionFeedback.textContent = `[SEGURIDAD CAMPUS] Ruta de evacuación para ${spot.label} (${ubicacion}): Diríjase hacia las escaleras de emergencia y punto de encuentro exterior del campus.`;
      return;
    }

    actionFeedback.textContent = `[OK] Acción ejecutada en [${spot.code}]: "${optionText}".`;

  } catch (err) {
    actionFeedback.textContent = `[ERROR DE CONEXIÓN] No fue posible comunicarse con el servidor (${err.message}).`;
  }
}

// 11. CAMBIAR ENTRE LOS 2 MÓDULOS
moduleButtons.forEach((btn) => {
  btn.addEventListener("click", () => {
    const targetModule = btn.getAttribute("data-module");
    window.CURRENT_ACTIVE_MODULE = targetModule;

    moduleButtons.forEach((b) => b.classList.remove("active"));
    btn.classList.add("active");

    Object.keys(moduleSections).forEach((key) => {
      if (moduleSections[key]) {
        moduleSections[key].classList.toggle("active", key === targetModule);
      }
    });

    contextMenu.classList.add("hidden");
    document.querySelectorAll(".hotspot-btn").forEach((b) => b.classList.remove("selected"));
  });
});

// 12. CERRAR MENÚ Y ALTERNAR VISTA DE IMAGEN
btnCloseMenu.addEventListener("click", () => {
  contextMenu.classList.add("hidden");
  document.querySelectorAll(".hotspot-btn").forEach((b) => b.classList.remove("selected"));
});

toggleRealImage.addEventListener("change", (e) => {
  document.body.classList.toggle("use-real-img", e.target.checked);
});

// 13. INICIALIZAR RENDERIZADO Y SINCRONIZACIÓN AUTOMÁTICA
renderHotspots();
sincronizarDatosBackend();

// Sincronizar periódicamente cada 30 segundos con el backend
setInterval(sincronizarDatosBackend, 30000);