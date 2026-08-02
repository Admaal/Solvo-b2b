package com.helpdesk.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelpdeskRuntimeSecretsEnvironmentPostProcessorTest {

    private final HelpdeskRuntimeSecretsEnvironmentPostProcessor processor =
            new HelpdeskRuntimeSecretsEnvironmentPostProcessor();

    @Test
    void expandeJsonEnPropiedadesIndividuales() throws Exception {
        String json = new ObjectMapper().writeValueAsString(java.util.Map.of(
                "DATABASE_URL", "jdbc:postgresql://db.example/test",
                "DATABASE_USERNAME", "user",
                "DATABASE_PASSWORD", "pass",
                "JWT_SECRET", "secret-with-at-least-32-characters-long"
        ));

        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("HELPDESK_RUNTIME_JSON", json);

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals("jdbc:postgresql://db.example/test", environment.getProperty("DATABASE_URL"));
        assertEquals("user", environment.getProperty("DATABASE_USERNAME"));
        assertEquals("pass", environment.getProperty("DATABASE_PASSWORD"));
        assertEquals("secret-with-at-least-32-characters-long", environment.getProperty("JWT_SECRET"));
    }

    @Test
    void ignoraSiNoHayJson() {
        StandardEnvironment environment = new StandardEnvironment();
        processor.postProcessEnvironment(environment, new SpringApplication());
        assertEquals(null, environment.getProperty("DATABASE_URL"));
    }
}
