package com.helpdesk.infrastructure.security;

import com.helpdesk.domain.model.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(UUID usuarioId, String email, Rol rol) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plusSeconds(jwtProperties.getExpirationMinutes() * 60);

        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("email", email)
                .claim("rol", rol.name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(secretKey)
                .compact();
    }

    public Claims parsearToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean esValido(String token) {
        try {
            Claims claims = parsearToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }
}
