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
 * Expande un único secret JSON ({@code HELPDESK_RUNTIME_JSON}) en propiedades
 * de entorno estándar. En Render las variables se inyectan sueltas; este
 * procesador queda en no-op si el JSON no está presente.
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
            String[] required = {"DATABASE_URL", "DATABASE_USERNAME", "DATABASE_PASSWORD", "JWT_SECRET"};
            Map<String, Object> props = new HashMap<>();
            for (String key : required) {
                if (!node.hasNonNull(key) || node.get(key).asText().isBlank()) {
                    throw new IllegalStateException("HELPDESK_RUNTIME_JSON no incluye " + key);
                }
                props.put(key, node.get(key).asText());
            }
            environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, props));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo parsear HELPDESK_RUNTIME_JSON", e);
        }
    }
}
