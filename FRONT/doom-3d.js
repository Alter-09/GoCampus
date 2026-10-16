/**
 * MOTOR 3D ESTILO DOOM PARA CAMPUSLANDS (Three.js Vanilla)
 * Genera las salas leyendo los planos SVG del DOM + Hotspots + Mascota Orbit
 */
(function () {
  // Referencias del DOM
  const btnStart3D = document.getElementById("btn-start-3d");
  const btnExit3D = document.getElementById("btn-exit-3d");
  const btnSwitchModule3D = document.getElementById("btn-3d-switch-module");
  const btnCallOrbit3D = document.getElementById("btn-3d-call-orbit");
  const mainWorkspace = document.getElementById("main-workspace");
  const doomModal = document.getElementById("doom-3d-modal");
  const doomViewport = document.getElementById("doom-viewport");
  const doomCanvas = document.getElementById("doom-canvas");
  const hotspotsOverlay = document.getElementById("doom-hotspots-overlay");

  // Elementos del Tooltip 3D y HUD
  const doomTooltip = document.getElementById("doom-tooltip");
  const ttCode = document.getElementById("doom-tt-code");
  const ttTitle = document.getElementById("doom-tt-title");
  const ttDesc = document.getElementById("doom-tt-desc");
  const ttCap = document.getElementById("doom-tt-cap");
  const ttStatus = document.getElementById("doom-tt-status");
  const ttActions = document.getElementById("doom-tt-actions");
  const ttClose = document.getElementById("doom-tt-close");

  const hudTitle = document.getElementById("doom-hud-title");
  const hudSpeed = document.getElementById("hud-speed");
  const hudSectorCode = document.getElementById("hud-sector-code");
  const hudOrbitSpeech = document.getElementById("hud-orbit-speech");
  const hudOrbitVisor = document.getElementById("hud-orbit-visor");
  const hudHotspotsCount = document.getElementById("hud-hotspots-count");
  const hudMotionMode = document.getElementById("hud-motion-mode");

  // Detección reactiva de accesibilidad prefers-reduced-motion
  const reducedMotionQuery = window.matchMedia("(prefers-reduced-motion: reduce)");
  let prefersReducedMotion = reducedMotionQuery.matches;
  reducedMotionQuery.addEventListener("change", (e) => {
    prefersReducedMotion = e.matches;
    updateMotionHudLabel();
  });

  function updateMotionHudLabel() {
    if (hudMotionMode) {
      hudMotionMode.textContent = prefersReducedMotion ? "ESTÁTICO (A11Y)" : "DOOM FPS";
    }
  }

  // Variables de estado de Three.js y físicas DOOM
  let scene = null;
  let camera = null;
  let renderer = null;
  let animationFrameId = null;
  let is3DActive = false;
  let isPointerLocked = false;

  // Escala de conversión SVG -> Mundo 3D
  const WORLD_WIDTH = 120;
  const WALL_HEIGHT = 12;
  const EYE_HEIGHT = 5.2;

  // Físicas estilo DOOM (Velocidad, Inercia, Fricción, Head-bobbing)
  const player = {
    yaw: 0,
    pitch: 0,
    velocity: new THREE.Vector3(),
    moveInput: { forward: 0, right: 0, turnLeft: 0, turnRight: 0 },
    accel: 135.0,
    friction: 7.5,
    maxSpeed: 28.0,
    bobTimer: 0,
    targetLookAt: null,
    targetPos: null
  };

  // Listas de objetos interactivos en la escena actual
  let hotspotNodes = [];
  let orbitSprite = null;
  let orbitGreetingTimer = 0;

  // =========================================================================
  // 1. GENERADOR DE TEXTURAS PIXEL-ART RETRO (DOOM TECHBASE + ORBIT + MARCA)
  // =========================================================================
  function createPixelTexture(drawFn, width = 64, height = 64, repeatX = 1, repeatY = 1) {
    const c = document.createElement("canvas");
    c.width = width;
    c.height = height;
    const ctx = c.getContext("2d");
    ctx.imageSmoothingEnabled = false;
    drawFn(ctx, width, height);

    const tex = new THREE.CanvasTexture(c);
    tex.magFilter = THREE.NearestFilter;
    tex.minFilter = THREE.NearestFilter;
    tex.wrapS = THREE.RepeatWrapping;
    tex.wrapT = THREE.RepeatWrapping;
    tex.repeat.set(repeatX, repeatY);
    return tex;
  }

  // Textura de Pared Industrial DOOM con colores Campuslands (#07102B, #2C67BA, #2CAAFF)
  function getDoomWallTexture(isBrandWall = false) {
    return createPixelTexture((ctx, w, h) => {
      // Fondo base Azul Petróleo
      ctx.fillStyle = "#07102B";
      ctx.fillRect(0, 0, w, h);

      // Paneles metálicos remachados con ruido grunge
      for (let y = 0; y < h; y += 4) {
        for (let x = 0; x < w; x += 4) {
          const noise = ((x * 19 + y * 37) % 16) - 8;
          const r = Math.max(0, Math.min(255, 16 + noise));
          const g = Math.max(0, Math.min(255, 28 + noise));
          const b = Math.max(0, Math.min(255, 58 + noise));
          ctx.fillStyle = `rgb(${r},${g},${b})`;
          ctx.fillRect(x, y, 4, 4);
        }
      }

      // Franja luminosa horizontal estilo estación espacial
      ctx.fillStyle = "#2C67BA";
      ctx.fillRect(0, 10, w, 6);
      ctx.fillStyle = "#2CAAFF";
      ctx.fillRect(0, 12, w, 2);

      // Borde inferior de advertencia industrial
      ctx.fillStyle = "#111111";
      ctx.fillRect(0, h - 8, w, 8);
      ctx.fillStyle = "#00AA80";
      for (let i = 0; i < w; i += 8) {
        ctx.fillRect(i, h - 6, 4, 4);
      }

      // Costuras verticales de placas DOOM
      ctx.fillStyle = "#040817";
      ctx.fillRect(0, 0, 2, h);
      ctx.fillRect(w / 2, 0, 2, h);

      // Si es pared de marca, estampa el casco de Campuslands pixelado
      if (isBrandWall) {
        ctx.fillStyle = "#000051";
        ctx.fillRect(10, 20, 44, 28);
        ctx.strokeStyle = "#2CAAFF";
        ctx.lineWidth = 2;
        ctx.strokeRect(10, 20, 44, 28);

        // Casco blanco/celeste en el centro
        ctx.fillStyle = "#FFFFFF";
        ctx.fillRect(22, 24, 20, 14);
        ctx.fillStyle = "#07102B";
        ctx.fillRect(25, 27, 14, 8);
        ctx.fillStyle = "#57BBFF";
        ctx.fillRect(18, 41, 28, 4);
      }
    }, 64, 64, 2, 1);
  }

  // Textura de Suelo y Techo Pixelada
  function getDoomFloorTexture() {
    return createPixelTexture((ctx, w, h) => {
      ctx.fillStyle = "#0b132b";
      ctx.fillRect(0, 0, w, h);
      ctx.strokeStyle = "#1e293b";
      ctx.lineWidth = 2;
      ctx.strokeRect(1, 1, w - 2, h - 2);
      ctx.fillStyle = "#2C67BA";
      ctx.fillRect(6, 6, 3, 3);
      ctx.fillRect(w - 9, 6, 3, 3);
      ctx.fillRect(6, h - 9, 3, 3);
      ctx.fillRect(w - 9, h - 9, 3, 3);
    }, 64, 64, 24, 24);
  }

  // Textura Sprite de la Mascota Oficial: ORBIT (Astronauta Campuslands)
  function getOrbitMascotTexture() {
    return createPixelTexture((ctx, w, h) => {
      ctx.clearRect(0, 0, w, h);

      // Mochila espacial blanca detrás
      ctx.fillStyle = "#dce4ec";
      ctx.fillRect(14, 18, 36, 26);

      // Casco redondeado blanco con bordes azules
      ctx.fillStyle = "#ffffff";
      ctx.fillRect(18, 4, 28, 22);
      ctx.fillStyle = "#2C67BA";
      ctx.fillRect(15, 11, 3, 9);
      ctx.fillRect(46, 11, 3, 9);

      // Visor negro con sonrisa y ojos LED en Azul Claro (#2CAAFF)
      ctx.fillStyle = "#07102B";
      ctx.fillRect(21, 7, 22, 15);
      ctx.fillStyle = "#2CAAFF";
      // Ojos felices ^ ^
      ctx.fillRect(25, 10, 3, 2);
      ctx.fillRect(36, 10, 3, 2);
      // Sonrisa LED pixelada
      ctx.fillRect(25, 15, 14, 2);
      ctx.fillRect(23, 13, 2, 2);
      ctx.fillRect(39, 13, 2, 2);

      // Torso del traje blanco con arnés azul (#2C67BA)
      ctx.fillStyle = "#f8fafc";
      ctx.fillRect(18, 26, 28, 20);
      ctx.fillStyle = "#2C67BA";
      ctx.fillRect(21, 26, 4, 20);
      ctx.fillRect(39, 26, 4, 20);
      ctx.fillRect(18, 41, 28, 4);

      // Panel de control en el pecho
      ctx.fillStyle = "#64748b";
      ctx.fillRect(25, 30, 14, 9);
      ctx.fillStyle = "#00AA80";
      ctx.fillRect(27, 32, 4, 4);

      // Brazos y Bandera de Colombia en el hombro (Amarillo, Azul, Rojo)
      ctx.fillStyle = "#ffffff";
      ctx.fillRect(11, 27, 7, 16);
      ctx.fillRect(46, 27, 7, 16);
      // Bandera Colombia
      ctx.fillStyle = "#facc15";
      ctx.fillRect(11, 29, 5, 2);
      ctx.fillStyle = "#1d4ed8";
      ctx.fillRect(11, 31, 5, 1);
      ctx.fillStyle = "#dc2626";
      ctx.fillRect(11, 32, 5, 1);

      // Piernas, rodilleras azules y botas
      ctx.fillStyle = "#f1f5f9";
      ctx.fillRect(20, 46, 10, 14);
      ctx.fillRect(34, 46, 10, 14);
      ctx.fillStyle = "#2C67BA";
      ctx.fillRect(20, 51, 10, 5);
      ctx.fillRect(34, 51, 10, 5);
      ctx.fillStyle = "#cbd5e1";
      ctx.fillRect(19, 59, 12, 5);
      ctx.fillRect(33, 59, 12, 5);
    }, 64, 64, 1, 1);
  }

  // =========================================================================
  // 2. CONSTRUCCIÓN DE LA SALA 3D BASADA EN EL RENDERIZADO SVG DEL DOM
  // =========================================================================
  function buildSceneFromActiveDOM(moduleKey) {
    // Limpiar escena previa si existe
    if (scene) {
      while (scene.children.length > 0) {
        scene.remove(scene.children[0]);
      }
    } else {
      scene = new THREE.Scene();
    }

    hotspotNodes = [];
    hotspotsOverlay.innerHTML = "";

    // Niebla y fondo oscuro retro estilo DOOM
    scene.background = new THREE.Color(0x050917);
    scene.fog = new THREE.FogExp2(0x050917, 0.014);

    // Iluminación ambiental + luces puntuales de neón de marca
    const ambientLight = new THREE.AmbientLight(0xdbeafe, 0.78);
    scene.add(ambientLight);

    const pointLight1 = new THREE.PointLight(0x2caaff, 1.2, 95);
    pointLight1.position.set(0, 10, 0);
    scene.add(pointLight1);

    const pointLight2 = new THREE.PointLight(0x00aa80, 0.9, 85);
    pointLight2.position.set(25, 9, -15);
    scene.add(pointLight2);

    // Localizar el SVG renderizado del módulo activo en el DOM
    const stageId = moduleKey === "canaveral" ? "stage-canaveral" : "stage-iss";
    const stageEl = document.getElementById(stageId);
    const svgEl = stageEl.querySelector("svg.rendered-blueprint");
    const vb = svgEl.viewBox.baseVal;
    const svgW = vb.width || 1280;
    const svgH = vb.height || 960;
    const worldDepth = WORLD_WIDTH * (svgH / svgW);

    // Materiales pixelados
    const wallMat = new THREE.MeshLambertMaterial({ map: getDoomWallTexture(false) });
    const brandWallMat = new THREE.MeshLambertMaterial({ map: getDoomWallTexture(true) });
    const floorMat = new THREE.MeshLambertMaterial({ map: getDoomFloorTexture() });
    const ceilingMat = new THREE.MeshBasicMaterial({
      color: 0x07102b,
      wireframe: false
    });

    // Suelo y Techo del Módulo
    const floorGeo = new THREE.PlaneGeometry(WORLD_WIDTH * 1.15, worldDepth * 1.15);
    const floorMesh = new THREE.Mesh(floorGeo, floorMat);
    floorMesh.rotation.x = -Math.PI / 2;
    scene.add(floorMesh);

    const ceilMesh = new THREE.Mesh(floorGeo, ceilingMat);
    ceilMesh.rotation.x = Math.PI / 2;
    ceilMesh.position.y = WALL_HEIGHT;
    scene.add(ceilMesh);

    // Muros Perimetrales de Contención de la Estación
    const perimeterThickness = 2;
    createWallBox(0, 0, WORLD_WIDTH, perimeterThickness, WALL_HEIGHT, brandWallMat, 0, -worldDepth / 2);
    createWallBox(0, 0, WORLD_WIDTH, perimeterThickness, WALL_HEIGHT, brandWallMat, 0, worldDepth / 2);
    createWallBox(0, 0, perimeterThickness, worldDepth, WALL_HEIGHT, wallMat, -WORLD_WIDTH / 2, 0);
    createWallBox(0, 0, perimeterThickness, worldDepth, WALL_HEIGHT, wallMat, WORLD_WIDTH / 2, 0);

    // Lectura dinámica de los <rect> del SVG para levantar salas, cubículos y bloques
    const rects = svgEl.querySelectorAll("rect");
    rects.forEach((r, idx) => {
      const rx = parseFloat(r.getAttribute("x") || 0);
      const ry = parseFloat(r.getAttribute("y") || 0);
      const rw = parseFloat(r.getAttribute("width") || 0);
      const rh = parseFloat(r.getAttribute("height") || 0);

      // Ignorar la placa de fondo gigante (que abarca casi todo el SVG)
      if (rw > svgW * 0.85 && rh > svgH * 0.85) return;

      const w3d = (rw / svgW) * WORLD_WIDTH;
      const d3d = (rh / svgH) * worldDepth;
      const x3d = ((rx + rw / 2) / svgW - 0.5) * WORLD_WIDTH;
      const z3d = ((ry + rh / 2) / svgH - 0.5) * worldDepth;

      // Si es una habitación grande, creamos sus 4 paredes con una puerta de paso estilo DOOM
      if (w3d > 14 && d3d > 10) {
        buildRoomWallsWithDoorway(x3d, z3d, w3d, d3d, idx % 2 === 0 ? brandWallMat : wallMat);
      } else {
        // Si es un cubículo, mesa técnica o bloque compacto, creamos un módulo a media altura o columna
        const blockHeight = w3d < 4 ? WALL_HEIGHT * 0.85 : 3.4;
        const boxGeo = new THREE.BoxGeometry(w3d, blockHeight, d3d);
        const boxMesh = new THREE.Mesh(boxGeo, idx % 3 === 0 ? brandWallMat : wallMat);
        boxMesh.position.set(x3d, blockHeight / 2, z3d);
        scene.add(boxMesh);
      }
    });

    // Lectura dinámica de los <circle> del SVG (Islas Hexagonales / Mesas Colaborativas y Columnas)
    const circles = svgEl.querySelectorAll("circle");
    const islandMat = new THREE.MeshLambertMaterial({ color: 0x2c67ba });
    circles.forEach((c) => {
      const cx = parseFloat(c.getAttribute("cx") || 0);
      const cy = parseFloat(c.getAttribute("cy") || 0);
      const cr = parseFloat(c.getAttribute("r") || 10);

      const x3d = (cx / svgW - 0.5) * WORLD_WIDTH;
      const z3d = (cy / svgH - 0.5) * worldDepth;
      const r3d = Math.max(1.2, (cr / svgW) * WORLD_WIDTH);

      const cylGeo = new THREE.CylinderGeometry(r3d, r3d, 2.2, 6);
      const cylMesh = new THREE.Mesh(cylGeo, islandMat);
      cylMesh.position.set(x3d, 1.1, z3d);
      scene.add(cylMesh);
    });

    // Añadir a ORBIT (Mascota Oficial Campuslands) como Sprite 3D en el centro del sector
    const orbitMaterial = new THREE.SpriteMaterial({ map: getOrbitMascotTexture() });
    orbitSprite = new THREE.Sprite(orbitMaterial);
    orbitSprite.position.set(4, 3.6, 2);
    orbitSprite.scale.set(6.5, 6.5, 1);
    scene.add(orbitSprite);

    // Registrar a Orbit también como punto interactivo accesible
    createHotspotNode({
      id: "orbit-mascot",
      number: "👩‍🚀",
      label: "Mascota Oficial: ORBIT",
      code: "CL-ORBIT-00",
      capacity: "Guía Espacial",
      status: "En línea",
      description: "¡Hola Camper! Soy Orbit, el astronauta oficial de Campuslands. Selecciona cualquier sector o usa WASD para recorrer la base.",
      menuOptions: [
        "Recibir saludo de Orbit",
        "Ver misión de Campuslands",
        "Consultar ruta de evacuación general"
      ],
      isOrbit: true
    }, orbitSprite.position.clone().add(new THREE.Vector3(0, 2.6, 0)));

    // Crear los Hotspots 3D a partir de MODULES_DATA del proyecto original
    const moduleData = window.MODULES_DATA[moduleKey];
    moduleData.hotspots.forEach((spot) => {
      const hx = (spot.left / 100 - 0.5) * WORLD_WIDTH;
      const hz = (spot.top / 100 - 0.5) * worldDepth;

      // Baliza 3D flotante estilo DOOM (Octaedro energético)
      const beaconGeo = new THREE.OctahedronGeometry(1.15, 0);
      const beaconMat = new THREE.MeshBasicMaterial({
        color: 0x2caaff,
        wireframe: false
      });
      const beaconMesh = new THREE.Mesh(beaconGeo, beaconMat);
      beaconMesh.position.set(hx, 4.2, hz);
      scene.add(beaconMesh);

      // Pedestal bajo el hotspot
      const pedGeo = new THREE.CylinderGeometry(0.45, 0.75, 1.6, 8);
      const pedMat = new THREE.MeshLambertMaterial({ color: 0x990012 });
      const pedMesh = new THREE.Mesh(pedGeo, pedMat);
      pedMesh.position.set(hx, 0.8, hz);
      scene.add(pedMesh);

      createHotspotNode(spot, beaconMesh.position, beaconMesh);
    });

    // Actualizar HUD
    hudTitle.textContent = `${moduleData.moduleName} — Exploración 3D`;
    hudSectorCode.textContent = moduleKey === "canaveral" ? "CAÑAVERAL" : "ISS CAMPUS";
    hudHotspotsCount.textContent = `${moduleData.hotspots.length} ACTIVOS`;
    updateMotionHudLabel();

    // Posicionar cámara al inicio del pasillo principal
    camera.position.set(0, EYE_HEIGHT, worldDepth * 0.28);
    player.yaw = 0;
    player.pitch = 0;
    player.velocity.set(0, 0, 0);
    player.targetLookAt = null;
    player.targetPos = null;
  }

  function createWallBox(x, y, w, d, h, mat, posX, posZ) {
    const geo = new THREE.BoxGeometry(w, h, d);
    const mesh = new THREE.Mesh(geo, mat);
    mesh.position.set(posX, h / 2, posZ);
    scene.add(mesh);
  }

  // Construye las paredes de un salón dejando vanos (puertas abiertas) para poder entrar caminando
  function buildRoomWallsWithDoorway(cx, cz, w, d, mat) {
    const t = 0.65;
    const h = WALL_HEIGHT * 0.92;
    // Pared Norte y Sur (con abertura central para puerta)
    const segW = Math.max(1.5, (w - 5.5) / 2);
    createWallBox(0, 0, segW, t, h, mat, cx - w / 2 + segW / 2, cz - d / 2);
    createWallBox(0, 0, segW, t, h, mat, cx + w / 2 - segW / 2, cz - d / 2);
    createWallBox(0, 0, segW, t, h, mat, cx - w / 2 + segW / 2, cz + d / 2);
    createWallBox(0, 0, segW, t, h, mat, cx + w / 2 - segW / 2, cz + d / 2);

    // Pared Este y Oeste
    createWallBox(0, 0, t, d, h, mat, cx - w / 2, cz);
    createWallBox(0, 0, t, d, h, mat, cx + w / 2, cz);
  }

  // =========================================================================
  // 3. CREACIÓN Y LÓGICA DE HOTSPOTS 3D ACCESIBLES (TECLADO + RATÓN)
  // =========================================================================
  function createHotspotNode(spot, worldPosition, meshRef = null) {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = `doom-hotspot-3d ${spot.isOrbit ? "orbit-hotspot" : ""}`;
    btn.setAttribute("aria-label", `Punto 3D ${spot.number}: ${spot.label}. Presiona Enter para inspeccionar.`);
    btn.innerHTML = `<span class="spot-badge">${spot.number}</span> <span>${spot.label}</span>`;

    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      if (document.pointerLockElement === doomCanvas) {
        document.exitPointerLock();
      }
      activateHotspot3D(spot, worldPosition, btn);
    });

    hotspotsOverlay.appendChild(btn);
    hotspotNodes.push({
      spot,
      worldPos: worldPosition.clone(),
      domBtn: btn,
      mesh: meshRef
    });
  }

  function activateHotspot3D(spot, worldPos, btnEl) {
    document.querySelectorAll(".doom-hotspot-3d").forEach((b) => b.classList.remove("active-spot"));
    if (btnEl) btnEl.classList.add("active-spot");

    // Si el usuario NO tiene prefers-reduced-motion, desplazamos suavemente la cámara hacia el punto;
    // si tiene prefers-reduced-motion activo, orientamos de forma instantánea sin mareo.
    const offsetPos = worldPos.clone().add(new THREE.Vector3(0, 1.0, 9.5));
    if (prefersReducedMotion) {
      camera.position.set(offsetPos.x, EYE_HEIGHT, offsetPos.z);
      camera.lookAt(worldPos.x, EYE_HEIGHT, worldPos.z);
      const euler = new THREE.Euler().setFromQuaternion(camera.quaternion, "YXZ");
      player.yaw = euler.y;
      player.pitch = euler.x;
      player.targetPos = null;
      player.targetLookAt = null;
    } else {
      player.targetPos = new THREE.Vector3(offsetPos.x, EYE_HEIGHT, offsetPos.z);
      player.targetLookAt = new THREE.Vector3(worldPos.x, EYE_HEIGHT * 0.85, worldPos.z);
    }

    // Si es Orbit o se saluda a Orbit, disparamos animación de saludo
    if (spot.isOrbit) {
      triggerOrbitSalute("¡Bienvenido a bordo, Camper! Explora cada sala con confianza.");
    } else {
      hudOrbitSpeech.textContent = `ORBIT: "Inspeccionando [${spot.code}]"`;
    }

    // Mostrar Tooltip accesible con alto contraste WCAG AA
    ttCode.textContent = spot.code;
    ttTitle.textContent = spot.label;
    ttDesc.textContent = spot.description;
    ttCap.textContent = spot.capacity;
    ttStatus.textContent = spot.status;

    ttActions.innerHTML = "";
    spot.menuOptions.forEach((opt) => {
      const actBtn = document.createElement("button");
      actBtn.type = "button";
      actBtn.className = "doom-tt-action-btn";
      actBtn.innerHTML = `<span>${opt}</span> <span>&rarr;</span>`;
      actBtn.addEventListener("click", () => {
        triggerOrbitSalute(`¡Comando "${opt}" ejecutado en ${spot.code}!`);
      });
      ttActions.appendChild(actBtn);
    });

    doomTooltip.classList.remove("hidden");
  }

  function triggerOrbitSalute(message) {
    orbitGreetingTimer = 2.2;
    hudOrbitSpeech.textContent = `ORBIT: "${message}"`;
    hudOrbitVisor.innerHTML = `<span class="visor-eyes">★ ★</span><span class="visor-mouth">▀▀▀</span>`;
    setTimeout(() => {
      hudOrbitVisor.innerHTML = `<span class="visor-eyes">^ ^</span><span class="visor-mouth">░░░</span>`;
    }, 2000);
  }

  // Proyectar coordenadas 3D de cada Hotspot a coordenadas 2D de pantalla en cada frame
  function updateHotspotsScreenProjection() {
    if (!camera || !doomViewport) return;
    const width = doomViewport.clientWidth;
    const height = doomViewport.clientHeight;
    const tempV = new THREE.Vector3();

    hotspotNodes.forEach((node) => {
      tempV.copy(node.worldPos);
      tempV.project(camera);

      // Verificar si está delante de la cámara
      if (tempV.z > 1) {
        node.domBtn.style.display = "none";
        return;
      }

      const x = (tempV.x * 0.5 + 0.5) * width;
      const y = (-(tempV.y * 0.5) + 0.5) * height;

      node.domBtn.style.display = "inline-flex";
      node.domBtn.style.left = `${x}px`;
      node.domBtn.style.top = `${y}px`;
    });
  }

  // =========================================================================
  // 4. BUCLE DE RENDERIZADO Y FÍSICAS CLÁSICAS DOOM (WASD + POINTER LOCK)
  // =========================================================================
  let lastTime = performance.now();

  function animate3D(now) {
    if (!is3DActive) return;
    animationFrameId = requestAnimationFrame(animate3D);

    const dt = Math.min(0.05, (now - lastTime) / 1000);
    lastTime = now;

    // Rotación con teclado si no se usa ratón
    const turnSpeed = 2.0;
    if (player.moveInput.turnLeft) player.yaw += turnSpeed * dt;
    if (player.moveInput.turnRight) player.yaw -= turnSpeed * dt;

    // Si hay una transición suave activa hacia un Hotspot
    if (player.targetPos && !prefersReducedMotion) {
      camera.position.lerp(player.targetPos, dt * 4.5);
      if (player.targetLookAt) {
        const targetQuat = new THREE.Quaternion();
        const m = new THREE.Matrix4().lookAt(camera.position, player.targetLookAt, new THREE.Vector3(0, 1, 0));
        targetQuat.setFromRotationMatrix(m);
        camera.quaternion.slerp(targetQuat, dt * 5.0);
        const e = new THREE.Euler().setFromQuaternion(camera.quaternion, "YXZ");
        player.yaw = e.y;
        player.pitch = e.x;
      }
      if (camera.position.distanceTo(player.targetPos) < 0.35) {
        player.targetPos = null;
        player.targetLookAt = null;
      }
    } else {
      // Dirección hacia adelante y lateral en el plano XZ
      const forward = new THREE.Vector3(-Math.sin(player.yaw), 0, -Math.cos(player.yaw)).normalize();
      const right = new THREE.Vector3(Math.cos(player.yaw), 0, -Math.sin(player.yaw)).normalize();

      const wishDir = new THREE.Vector3();
      wishDir.addScaledVector(forward, player.moveInput.forward);
      wishDir.addScaledVector(right, player.moveInput.right);
      if (wishDir.lengthSq() > 0) {
        wishDir.normalize();
        player.targetPos = null;
      }

      if (prefersReducedMotion) {
        // Modo Accesible (Sin inercia ni balanceo de cámara)
        camera.position.addScaledVector(wishDir, player.maxSpeed * 0.65 * dt);
        camera.position.y = EYE_HEIGHT;
        hudSpeed.textContent = wishDir.lengthSq() > 0 ? "ESTABLE" : "0%";
      } else {
        // Físicas DOOM 90s: Aceleración rápida + deslizamiento con fricción + Head-Bobbing
        player.velocity.addScaledVector(wishDir, player.accel * dt);

        // Aplicar fricción de suelo DOOM
        const damping = Math.exp(-player.friction * dt);
        player.velocity.multiplyScalar(damping);

        // Limitar velocidad máxima
        const currentSpeed = player.velocity.length();
        if (currentSpeed > player.maxSpeed) {
          player.velocity.multiplyScalar(player.maxSpeed / currentSpeed);
        }

        camera.position.addScaledVector(player.velocity, dt);

        // Efecto clásico Head-Bobbing senoidal de DOOM al caminar
        if (currentSpeed > 1.2) {
          player.bobTimer += dt * 11.5;
          camera.position.y = EYE_HEIGHT + Math.sin(player.bobTimer) * 0.28;
        } else {
          camera.position.y = THREE.MathUtils.lerp(camera.position.y, EYE_HEIGHT, dt * 8);
        }

        const speedPct = Math.min(100, Math.round((currentSpeed / player.maxSpeed) * 100));
        hudSpeed.textContent = `${speedPct}%`;
      }

      // Aplicar orientación Yaw / Pitch a la cámara
      const euler = new THREE.Euler(player.pitch, player.yaw, 0, "YXZ");
      camera.quaternion.setFromEuler(euler);
    }

    // Limitar posición del jugador dentro del perímetro del módulo
    camera.position.x = Math.max(-WORLD_WIDTH * 0.47, Math.min(WORLD_WIDTH * 0.47, camera.position.x));
    camera.position.z = Math.max(-42, Math.min(42, camera.position.z));

    // Animar octaedros de hotspots y saludo de Orbit (si no hay reducción de movimiento)
    if (!prefersReducedMotion) {
      hotspotNodes.forEach((n) => {
        if (n.mesh) {
          n.mesh.rotation.y += dt * 2.2;
          n.mesh.position.y = 4.2 + Math.sin(now * 0.004 + n.worldPos.x) * 0.35;
        }
      });

      if (orbitSprite) {
        if (orbitGreetingTimer > 0) {
          orbitGreetingTimer -= dt;
          orbitSprite.position.y = 3.6 + Math.abs(Math.sin(now * 0.015)) * 1.4;
        } else {
          orbitSprite.position.y = 3.6 + Math.sin(now * 0.003) * 0.25;
        }
      }
    }

    renderer.render(scene, camera);
    updateHotspotsScreenProjection();
  }

  // =========================================================================
  // 5. FLUJO DE NAVEGACIÓN (UX ESTADOS 1 A 4) Y EVENTOS DE CONTROLES
  // =========================================================================
  function openDoom3D() {
    is3DActive = true;

    // Estado 2: Ocultar vista 2D principal sin recargar y mostrar Sala 3D
    mainWorkspace.classList.add("hidden-view");
    mainWorkspace.setAttribute("aria-hidden", "true");

    doomModal.classList.remove("hidden");
    doomModal.setAttribute("aria-hidden", "false");
    doomCanvas.setAttribute("aria-hidden", "false");

    // Inicializar Renderer y Cámara si es la primera vez
    const width = doomViewport.clientWidth || window.innerWidth;
    const height = doomViewport.clientHeight || window.innerHeight - 130;

    if (!renderer) {
      renderer = new THREE.WebGLRenderer({
        canvas: doomCanvas,
        antialias: false,
        powerPreference: "high-performance"
      });
      // Resolución retro ligeramente reducida para reforzar el pixel-art de DOOM
      renderer.setPixelRatio(Math.min(window.devicePixelRatio, 1));
    }
    renderer.setSize(width, height);

    if (!camera) {
      camera = new THREE.PerspectiveCamera(70, width / height, 0.5, 220);
    } else {
      camera.aspect = width / height;
      camera.updateProjectionMatrix();
    }

    buildSceneFromActiveDOM(window.CURRENT_ACTIVE_MODULE || "canaveral");

    lastTime = performance.now();
    animationFrameId = requestAnimationFrame(animate3D);

    // Enfocar botón de salida para accesibilidad inmediata con teclado
    btnExit3D.focus();
  }

  function closeDoom3D() {
    // Estado 4: Pausar/destruir bucle 3D para ahorrar recursos y devolver a la vista 2D
    is3DActive = false;
    if (animationFrameId) {
      cancelAnimationFrame(animationFrameId);
      animationFrameId = null;
    }

    if (document.pointerLockElement === doomCanvas) {
      document.exitPointerLock();
    }

    doomTooltip.classList.add("hidden");
    doomModal.classList.add("hidden");
    doomModal.setAttribute("aria-hidden", "true");
    doomCanvas.setAttribute("aria-hidden", "true");

    mainWorkspace.classList.remove("hidden-view");
    mainWorkspace.setAttribute("aria-hidden", "false");

    // Devolver el foco al botón Recorrido 3D que abrió la experiencia
    btnStart3D.focus();
  }

  // Eventos de Botones Principales
  btnStart3D.addEventListener("click", openDoom3D);
  btnExit3D.addEventListener("click", closeDoom3D);

  btnSwitchModule3D.addEventListener("click", () => {
    window.CURRENT_ACTIVE_MODULE = window.CURRENT_ACTIVE_MODULE === "canaveral" ? "iss" : "canaveral";
    // Sincronizar también las pestañas 2D de fondo
    const targetBtn = document.querySelector(`.module-btn[data-module="${window.CURRENT_ACTIVE_MODULE}"]`);
    if (targetBtn) targetBtn.click();
    doomTooltip.classList.add("hidden");
    buildSceneFromActiveDOM(window.CURRENT_ACTIVE_MODULE);
  });

  btnCallOrbit3D.addEventListener("click", () => {
    const orbitNode = hotspotNodes.find((n) => n.spot.isOrbit);
    if (orbitNode) {
      activateHotspot3D(orbitNode.spot, orbitNode.worldPos, orbitNode.domBtn);
    }
  });

  ttClose.addEventListener("click", () => {
    doomTooltip.classList.add("hidden");
  });

  // Pointer Lock API al hacer clic en el lienzo 3D
  doomCanvas.addEventListener("click", () => {
    if (is3DActive && document.pointerLockElement !== doomCanvas) {
      doomCanvas.requestPointerLock();
    }
  });

  document.addEventListener("pointerlockchange", () => {
    isPointerLocked = document.pointerLockElement === doomCanvas;
  });

  document.addEventListener("mousemove", (e) => {
    if (!is3DActive || !isPointerLocked) return;
    const sensitivity = 0.0022;
    player.yaw -= e.movementX * sensitivity;
    player.pitch -= e.movementY * sensitivity;
    player.pitch = Math.max(-Math.PI / 3, Math.min(Math.PI / 3, player.pitch));
  });

  // Controles de Teclado (WASD + Flechas + Escape)
  window.addEventListener("keydown", (e) => {
    if (!is3DActive) return;

    switch (e.code) {
      case "KeyW":
      case "ArrowUp":
        player.moveInput.forward = 1;
        break;
      case "KeyS":
      case "ArrowDown":
        player.moveInput.forward = -1;
        break;
      case "KeyA":
        player.moveInput.right = -1;
        break;
      case "KeyD":
        player.moveInput.right = 1;
        break;
      case "ArrowLeft":
        player.moveInput.turnLeft = 1;
        break;
      case "ArrowRight":
        player.moveInput.turnRight = 1;
        break;
      case "Escape":
        if (!doomTooltip.classList.contains("hidden")) {
          doomTooltip.classList.add("hidden");
        } else if (!isPointerLocked) {
          closeDoom3D();
        }
        break;
    }
  });

  window.addEventListener("keyup", (e) => {
    if (!is3DActive) return;
    switch (e.code) {
      case "KeyW":
      case "ArrowUp":
      case "KeyS":
      case "ArrowDown":
        player.moveInput.forward = 0;
        break;
      case "KeyA":
      case "KeyD":
        player.moveInput.right = 0;
        break;
      case "ArrowLeft":
        player.moveInput.turnLeft = 0;
        break;
      case "ArrowRight":
        player.moveInput.turnRight = 0;
        break;
    }
  });

  // Ajuste responsivo del canvas 3D
  window.addEventListener("resize", () => {
    if (!is3DActive || !renderer || !camera) return;
    const w = doomViewport.clientWidth;
    const h = doomViewport.clientHeight;
    camera.aspect = w / h;
    camera.updateProjectionMatrix();
    renderer.setSize(w, h);
  });
})();