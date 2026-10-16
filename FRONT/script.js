// CONFIGURACIÓN DE LOS 2 MÓDULOS (CABINAS 4 Y 5 INDIVIDUALES + RECUADRO DESTACADO EN EL NUMERAL 7)
const MODULES_DATA = {
  canaveral: {
    moduleName: "Sector Cabo Cañaveral",
    containerId: "hotspots-canaveral",
    hotspots: [
      {
        id: "cc-domo",
        number: "1",
        label: "Domo Circular / Zona Lounge",
        code: "CC-DOMO-01",
        capacity: "35 personas",
        status: "Activo",
        left: 67.6,
        top: 22.3,
        size: 52,
        description: "Sector semicircular superior del Sector Cabo Cañaveral con mesas colaborativas y barra perimetral.",
        menuOptions: [
          "Ver disponibilidad de mesas",
          "Reservar domo para evento",
          "Consultar ruta de evacuación",
          "Inventario audiovisual"
        ]
      },
      {
        id: "cc-sala-central",
        number: "2",
        label: "Sala Operativa Central",
        code: "CC-SALA-02",
        capacity: "6 personas",
        status: "Ocupado",
        left: 65.8,
        top: 36.8,
        size: 26,
        description: "Espacio intermedio bajo el domo para trabajo focalizado o coordinación de staff.",
        menuOptions: [
          "Ver responsable actual",
          "Solicitar turno de uso",
          "Consultar conectividad"
        ]
      },
      {
        id: "cc-recuadro-negro-sup",
        number: "3",
        label: "Zona Destacada Superior (Recuadro Negro)",
        code: "CC-CTRL-03",
        capacity: "8 personas",
        status: "Prioritario",
        left: 71.5,
        top: 36.6,
        size: 28,
        description: "Área demarcada con recuadro negro en el plano. Punto estratégico de control y tránsito hacia el sector Hunters y el Domo.",
        menuOptions: [
          "Abrir panel de control del área",
          "Ver protocolo de acceso",
          "Ruta de evacuación inmediata",
          "Bitácora de novedades"
        ]
      },
      {
        id: "cc-cabina-izq",
        number: "4",
        label: "Cabina Focus Izquierda",
        code: "CC-CAB-04",
        capacity: "2 personas",
        status: "Disponible",
        left: 69.2,
        top: 56.8,
        size: 22,
        description: "Cabina individual/doble ubicada sobre la Sala de Reunión para llamadas o entrevistas remotas.",
        menuOptions: [
          "Reservar cabina (30 min)",
          "Verificar estado de red"
        ]
      },
      {
        id: "cc-cabina-der",
        number: "5",
        label: "Cabina Focus Derecha",
        code: "CC-CAB-05",
        capacity: "2 personas",
        status: "Disponible",
        left: 72.7,
        top: 56.8,
        size: 22,
        description: "Cabina derecha contigua sobre la Sala de Reunión para sesiones 1 a 1 o videollamadas.",
        menuOptions: [
          "Reservar cabina (30 min)",
          "Verificar estado de red"
        ]
      },
      {
        // BOTÓN EN SALA DE REUNIÓN (SOBRE LA MESA OVALADA)
        id: "cc-sala-reunion",
        number: "6",
        label: "Sala de Reunión Principal",
        code: "CC-REU-06",
        capacity: "12 personas",
        status: "Disponible",
        left: 71.5,
        top: 70.3,
        size: 28,
        description: "Sala de Reunión ejecutiva equipada con mesa ovalada central para juntas directivas, presentaciones y comités.",
        menuOptions: [
          "Reservar Sala de Reunión",
          "Ver agenda de juntas del día",
          "Activar pantalla / proyector",
          "Consultar ruta de evacuación"
        ]
      },
      {
        // SALA DESTACADA ENCERRADA CON RECUADRO NEGRO (MISMO TAMAÑO QUE BAÑOS INF. DER: 125x95)
        id: "cc-sala-destacada-inf",
        number: "7",
        label: "Sala Destacada de Operaciones (Recuadro Negro)",
        code: "CC-CTRL-07",
        capacity: "10 personas",
        status: "Disponible",
        left: 42.0,
        top: 55.2,
        size: 28,
        description: "Nueva sala demarcada con recuadro negro contigua a los baños inferiores y el pasillo de evacuación. Espacio de trabajo especializado y enlace operativo.",
        menuOptions: [
          "Ver disponibilidad de sala",
          "Reservar espacio operativo",
          "Consultar conectividad y tomas",
          "Ruta de evacuación asignada"
        ]
      },
      {
        id: "cc-cubiculo-sup",
        number: "8",
        label: "Cubículo Técnico Superior",
        code: "CC-CUB-08",
        capacity: "4 personas",
        status: "Activo",
        left: 42.0,
        top: 70.6,
        size: 22,
        description: "Módulo operativo superior ubicado en el ala centro-izquierda del piso.",
        menuOptions: [
          "Ver equipo asignado",
          "Inventario de puestos",
          "Ver salida de emergencia cercana"
        ]
      },
      {
        id: "cc-cubiculo-inf",
        number: "9",
        label: "Cubículo Técnico Inferior",
        code: "CC-CUB-09",
        capacity: "4 personas",
        status: "Activo",
        left: 42.1,
        top: 76.8,
        size: 22,
        description: "Módulo operativo inferior ubicado en el ala centro-izquierda del piso.",
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
        id: "iss-salon-3",
        number: "S3",
        label: "Salón 3 CAMPUS (Sup. Izquierdo)",
        code: "ISS-SALON-03",
        capacity: "36 campers",
        status: "En clase",
        left: 11.4,
        top: 35.8,
        size: 36,
        description: "Aula de formación superior izquierda de la Estación Espacial Internacional con disposición curva de estaciones de cómputo.",
        menuOptions: [
          "Ver listado de Campers y Trainer",
          "Horario de entrenamiento del salón",
          "Control de equipos y periféricos",
          "Ruta de evacuación asignada"
        ]
      },
      {
        id: "iss-salon-2",
        number: "S2",
        label: "Salón 2 CAMPUS (Sup. Central)",
        code: "ISS-SALON-02",
        capacity: "36 campers",
        status: "Disponible",
        left: 31.9,
        top: 34.2,
        size: 36,
        description: "Aula de formación superior central de la Estación Espacial Internacional con salida directa al pasillo de evacuación.",
        menuOptions: [
          "Ver listado de Campers y Trainer",
          "Horario de entrenamiento del salón",
          "Control de equipos y periféricos",
          "Ruta de evacuación asignada"
        ]
      },
      {
        id: "iss-salon-4",
        number: "S4",
        label: "Salón 4 CAMPUS (Inf. Izquierdo)",
        code: "ISS-SALON-04",
        capacity: "36 campers",
        status: "En clase",
        left: 10.8,
        top: 70.3,
        size: 36,
        description: "Aula de formación inferior izquierda de la Estación Espacial Internacional.",
        menuOptions: [
          "Ver listado de Campers y Trainer",
          "Horario de entrenamiento del salón",
          "Control de equipos y periféricos",
          "Ruta de evacuación asignada"
        ]
      },
      {
        id: "iss-salon-1",
        number: "S1",
        label: "Salón 1 CAMPUS (Inf. Central)",
        code: "ISS-SALON-01",
        capacity: "30 campers",
        status: "Activo",
        left: 35.5,
        top: 76.5,
        size: 36,
        description: "Aula inferior demarcada con línea divisoria superior negra/roja en la Estación Espacial Internacional.",
        menuOptions: [
          "Ver listado de Campers y Trainer",
          "Horario de entrenamiento del salón",
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
const menuActions = document.getElementById("menu-actions");
const actionFeedback = document.getElementById("action-feedback");
const btnCloseMenu = document.getElementById("btn-close-menu");
const toggleRealImage = document.getElementById("toggle-real-image");

// 1. RENDERIZAR LOS BOTONES ROJOS (HOTSPOTS) EN AMBOS MÓDULOS
function renderHotspots() {
  Object.keys(MODULES_DATA).forEach((moduleKey) => {
    const moduleInfo = MODULES_DATA[moduleKey];
    const container = document.getElementById(moduleInfo.containerId);
    if (!container) return;
    container.innerHTML = "";

    moduleInfo.hotspots.forEach((spot) => {
      const btn = document.createElement("button");
      btn.className = "hotspot-btn";
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

// 2. ABRIR EL MENÚ INTERACTIVO AL DAR CLIC EN UN PUNTO ROJO
function openSpotMenu(moduleName, spot, clickedButton) {
  document.querySelectorAll(".hotspot-btn").forEach((b) => b.classList.remove("selected"));
  clickedButton.classList.add("selected");

  menuModuleTag.textContent = moduleName;
  menuTitle.textContent = spot.label;
  menuDescription.textContent = spot.description;
  menuCode.textContent = spot.code;
  menuCapacity.textContent = spot.capacity;
  menuStatus.textContent = spot.status;

  actionFeedback.classList.add("hidden");
  actionFeedback.textContent = "";

  menuActions.innerHTML = "";
  spot.menuOptions.forEach((optionText) => {
    const li = document.createElement("li");
    const actionBtn = document.createElement("button");
    actionBtn.className = "menu-action-btn";
    actionBtn.innerHTML = `<span>${optionText}</span> <span>&rarr;</span>`;

    actionBtn.addEventListener("click", () => {
      actionFeedback.textContent = `✅ Acción ejecutada en [${spot.code}]: "${optionText}"`;
      actionFeedback.classList.remove("hidden");
    });

    li.appendChild(actionBtn);
    menuActions.appendChild(li);
  });

  contextMenu.classList.remove("hidden");
}

// 3. CAMBIAR ENTRE LOS 2 MÓDULOS
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

// 4. CERRAR MENÚ Y ALTERNAR VISTA DE IMAGEN ORIGINAL
btnCloseMenu.addEventListener("click", () => {
  contextMenu.classList.add("hidden");
  document.querySelectorAll(".hotspot-btn").forEach((b) => b.classList.remove("selected"));
});

toggleRealImage.addEventListener("change", (e) => {
  document.body.classList.toggle("use-real-img", e.target.checked);
});

// Inicializar al cargar
renderHotspots();