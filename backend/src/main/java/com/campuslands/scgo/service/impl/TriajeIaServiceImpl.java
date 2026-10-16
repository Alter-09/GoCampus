package com.campuslands.scgo.service.impl;

import com.campuslands.scgo.dto.TriajeIaResultadoDTO;
import com.campuslands.scgo.service.TriajeIaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TriajeIaServiceImpl implements TriajeIaService {

    @Value("${campuslands.ai.api-key:}")
    private String apiKey;

    @Value("${campuslands.ai.model:grok-2}")
    private String modeloIa;

    @Value("${campuslands.ai.base-url:https://api.x.ai/v1}")
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    @Override
    public TriajeIaResultadoDTO evaluarIncidencia(String titulo, String descripcion, String espacioNombre, String recursoNombre) {
        long inicioMs = System.currentTimeMillis();

        // 1. Si existe API Key configurada, intentar inferencia real LLM (Grok / xAI / OpenAI-compatible)
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                TriajeIaResultadoDTO resultadoLlm = invocarLlmReal(titulo, descripcion, espacioNombre, recursoNombre, inicioMs);
                if (resultadoLlm != null) {
                    return resultadoLlm;
                }
            } catch (Exception ex) {
                log.warn("Aviso al consultar API de Grok / IA externa ({}). Activando Fallback Heurístico Local.", ex.getMessage());
            }
        }

        // 2. Fallback Heurístico Local Determinístico (Robusto, sin fallos no controlados)
        return aplicarFallbackHeuristico(titulo, descripcion, espacioNombre, recursoNombre, inicioMs);
    }

    private TriajeIaResultadoDTO invocarLlmReal(String titulo, String descripcion, String espacio, String recurso, long inicioMs) throws Exception {
        String systemPrompt = "Eres el agente de triaje del Sistema Central de Campuslands. "
                + "Analiza el reporte de novedad y responde UNICAMENTE un objeto JSON estricto sin texto adicional con los campos: "
                + "\"categoria\" (\"infraestructura\"|\"tecnologia\"|\"logistica\"|\"bienestar\"|\"otro\"), "
                + "\"prioridad\" (\"baja\"|\"media\"|\"alta\"|\"critica\"), "
                + "\"nivel_confianza\" (número decimal entre 0 y 1), "
                + "\"justificacion_tecnica\" (motivo claro del riesgo), "
                + "\"accion_inmediata_sugerida\" (medida preventiva sugerida).";

        String userPrompt = String.format("Título: %s | Descripción: %s | Espacio: %s | Recurso: %s",
                titulo != null ? titulo : "",
                descripcion != null ? descripcion : "",
                espacio != null ? espacio : "N/A",
                recurso != null ? recurso : "N/A");

        // Construir endpoint OpenAI-compatible / Grok
        String endpoint = baseUrl;
        if (!endpoint.endsWith("/chat/completions")) {
            endpoint = endpoint.endsWith("/") ? endpoint + "chat/completions" : endpoint + "/chat/completions";
        }

        Map<String, Object> requestPayload = Map.of(
                "model", modeloIa,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.1
        );

        String jsonBody = objectMapper.writeValueAsString(requestPayload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey.trim())
                .timeout(Duration.ofSeconds(8))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 && response.body() != null) {
            long latencia = System.currentTimeMillis() - inicioMs;
            String body = response.body();
            JsonNode root = objectMapper.readTree(body);

            String content = "";
            if (root.has("choices") && root.path("choices").isArray() && !root.path("choices").isEmpty()) {
                content = root.path("choices").get(0).path("message").path("content").asText();
            } else if (root.has("categoria")) {
                content = body;
            }

            // Limpiar bloques markdown ```json ... ```
            String cleanJson = content.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            JsonNode parsed = objectMapper.readTree(cleanJson);

            String categoria = parsed.path("categoria").asText("tecnologia");
            String prioridad = parsed.path("prioridad").asText("media");
            double confianzaVal = parsed.path("nivel_confianza").asDouble(0.95);
            String justificacion = parsed.path("justificacion_tecnica").asText("Evaluación emitida por modelo Grok");
            String accion = parsed.path("accion_inmediata_sugerida").asText("Revisión técnica asignada a mesa de ayuda");

            return TriajeIaResultadoDTO.builder()
                    .categoria(categoria)
                    .prioridad(prioridad)
                    .nivelConfianza(BigDecimal.valueOf(confianzaVal))
                    .justificacionTecnica(justificacion)
                    .accionInmediataSugerida(accion)
                    .modeloIa(modeloIa)
                    .latenciaMs((int) latencia)
                    .respuestaRaw(body)
                    .build();
        } else {
            log.warn("Respuesta no exitosa de Grok/LLM (HTTP {}): {}", response.statusCode(), response.body());
        }

        return null;
    }

    private TriajeIaResultadoDTO aplicarFallbackHeuristico(String titulo, String descripcion, String espacio, String recurso, long inicioMs) {
        String texto = ((titulo != null ? titulo : "") + " " + (descripcion != null ? descripcion : "")).toLowerCase();

        String categoria = "otro";
        String prioridad = "media";
        BigDecimal confianza = new BigDecimal("0.85");
        String justificacion;
        String accion;

        if (texto.contains("chispa") || texto.contains("humo") || texto.contains("fuego") || texto.contains("inundaci")
                || texto.contains("cortocircuito") || texto.contains("corto") || texto.contains("electrocut")
                || texto.contains("quema") || texto.contains("emergencia")) {
            categoria = "infraestructura";
            prioridad = "critica";
            confianza = new BigDecimal("0.98");
            justificacion = "Riesgo inminente detectado contra la integridad física de las personas o instalaciones del campus.";
            accion = "Evacuar o inhabilitar inmediatamente el espacio físico y cortar suministro eléctrico preventivo.";
        } else if (texto.contains("servidor") || texto.contains("switch") || texto.contains("caida de red") || texto.contains("sin internet")
                || texto.contains("pantalla azul") || texto.contains("red caida") || texto.contains("rack") || texto.contains("fibra")) {
            categoria = "tecnologia";
            prioridad = "alta";
            confianza = new BigDecimal("0.94");
            justificacion = "Afectación de infraestructura tecnológica neurálgica que compromete las clases de cohortes.";
            accion = "Despachar técnico de soporte de infraestructura de redes para diagnóstico inmediato.";
        } else if (texto.contains("mouse") || texto.contains("teclado") || texto.contains("cable") || texto.contains("hdmi")
                || texto.contains("pantalla") || texto.contains("proyector") || texto.contains("adaptador") || recurso != null) {
            categoria = "tecnologia";
            prioridad = "media";
            confianza = new BigDecimal("0.90");
            justificacion = "Falla de periférico o dispositivo audiovisual en puesto de trabajo.";
            accion = "Reemplazar periférico por una unidad operativa del almacén de soporte.";
        } else if (texto.contains("aire") || texto.contains("calor") || texto.contains("frio") || texto.contains("luz")
                || texto.contains("bombillo") || texto.contains("silla") || texto.contains("mesa") || texto.contains("puerta")) {
            categoria = "infraestructura";
            prioridad = "baja";
            confianza = new BigDecimal("0.88");
            justificacion = "Novedad locativa de confort o mantenimiento general del mobiliario.";
            accion = "Programar revisión con el equipo de servicios generales en la siguiente ventana de mantenimiento.";
        } else if (texto.contains("ruido") || texto.contains("convivencia") || texto.contains("primeros auxilios") || texto.contains("salud")) {
            categoria = "bienestar";
            prioridad = "media";
            confianza = new BigDecimal("0.92");
            justificacion = "Novedad relacionada con la armonía, clima educativo o bienestar estudiantil.";
            accion = "Contactar al líder de bienestar y coordinación formativa.";
        } else {
            justificacion = "Novedad operacional estándar reportada en campus.";
            accion = "Asignación estándar a mesa de ayuda para evaluación.";
        }

        long latencia = System.currentTimeMillis() - inicioMs;

        String rawJson = String.format("""
            {
              "categoria": "%s",
              "prioridad": "%s",
              "nivel_confianza": %s,
              "justificacion_tecnica": "%s",
              "accion_inmediata_sugerida": "%s",
              "motor": "grok_fallback_local"
            }
            """, categoria, prioridad, confianza, justificacion, accion);

        return TriajeIaResultadoDTO.builder()
                .categoria(categoria)
                .prioridad(prioridad)
                .nivelConfianza(confianza)
                .justificacionTecnica(justificacion)
                .accionInmediataSugerida(accion)
                .modeloIa(modeloIa)
                .latenciaMs((int) Math.max(latencia, 5))
                .respuestaRaw(rawJson)
                .build();
    }
}
