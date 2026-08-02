package com.helpdesk.domain.model;

import java.util.Objects;
import java.util.UUID;

public record UsuarioId(UUID value) {

    public UsuarioId {
        Objects.requireNonNull(value, "El id de usuario no puede ser nulo");
    }

    public static UsuarioId of(UUID value) {
        return new UsuarioId(value);
    }
}
