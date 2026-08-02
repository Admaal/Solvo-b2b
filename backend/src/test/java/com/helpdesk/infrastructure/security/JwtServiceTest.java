package com.helpdesk.infrastructure.security;

import com.helpdesk.domain.model.Rol;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-with-at-least-32-characters!!");
        properties.setExpirationMinutes(60);
        jwtService = new JwtService(properties);
    }

    @Test
    void generaYValidaToken() {
        UUID usuarioId = UUID.randomUUID();
        String token = jwtService.generarToken(usuarioId, "gestor@banco.test", Rol.GESTOR);

        assertTrue(jwtService.esValido(token));

        Claims claims = jwtService.parsearToken(token);
        assertEquals(usuarioId.toString(), claims.getSubject());
        assertEquals("gestor@banco.test", claims.get("email", String.class));
        assertEquals("GESTOR", claims.get("rol", String.class));
    }

    @Test
    void rechazaTokenInvalido() {
        assertFalse(jwtService.esValido("token.invalido"));
    }
}
