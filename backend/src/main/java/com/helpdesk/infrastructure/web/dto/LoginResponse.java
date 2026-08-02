package com.helpdesk.infrastructure.web.dto;

import java.util.UUID;

public record LoginResponse(
        String token,
        String email,
        String rol,
        UUID usuarioId,
        UUID organizacionId
) {
}
