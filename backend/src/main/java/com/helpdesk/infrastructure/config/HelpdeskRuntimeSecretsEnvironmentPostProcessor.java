package com.helpdesk.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Expande un único secret JSON de Cloud Run ({@code HELPDESK_RUNTIME_JSON})
 * en propiedades de entorno estándar usadas por Spring Boot.
 */
public class HelpdeskRuntimeSecretsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE_NAME = "helpdeskRuntimeSecrets";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String json = environment.getProperty("HELPDESK_RUNTIME_JSON");
        if (json == null || json.isBlank()) {
            return;
        }

        try {
            JsonNode node = MAPPER.readTree(json);
            Map<String, Object> props = new HashMap<>();
            mapIfPresent(node, props, "DATABASE_URL");
            mapIfPresent(node, props, "DATABASE_USERNAME");
            mapIfPresent(node, props, "DATABASE_PASSWORD");
            mapIfPresent(node, props, "JWT_SECRET");

            if (!props.isEmpty()) {
                environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, props));
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo parsear HELPDESK_RUNTIME_JSON", e);
        }
    }

    private static void mapIfPresent(JsonNode node, Map<String, Object> props, String key) {
        if (node.hasNonNull(key) && !node.get(key).asText().isBlank()) {
            props.put(key, node.get(key).asText());
        }
    }
}
