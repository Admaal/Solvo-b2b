package com.helpdesk.infrastructure.config;

import com.helpdesk.infrastructure.security.JwtProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProdJwtSecretGuard implements ApplicationRunner {

    static final String DEFAULT_SECRET = "change-me-in-production-use-at-least-32-chars!!";

    private final JwtProperties jwtProperties;

    public ProdJwtSecretGuard(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isBlank() || DEFAULT_SECRET.equals(secret) || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET de producción ausente, demasiado corto o igual al valor por defecto"
            );
        }
    }
}
