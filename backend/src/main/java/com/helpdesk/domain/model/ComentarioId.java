package com.helpdesk.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ComentarioId(UUID value) {

    public ComentarioId {
        Objects.requireNonNull(value, "El id del comentario no puede ser nulo");
    }

    public static ComentarioId nuevo() {
        return new ComentarioId(UUID.randomUUID());
    }

    public static ComentarioId of(UUID value) {
        return new ComentarioId(value);
    }
}
