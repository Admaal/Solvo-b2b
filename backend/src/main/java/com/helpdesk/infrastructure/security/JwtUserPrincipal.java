package com.helpdesk.infrastructure.security;

import com.helpdesk.domain.model.Rol;

import java.util.UUID;

public record JwtUserPrincipal(UUID usuarioId, String email, Rol rol) {
}
