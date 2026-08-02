package com.helpdesk.domain.model;

import java.util.Objects;
import java.util.UUID;

public record EquipoId(UUID value) {

    public EquipoId {
        Objects.requireNonNull(value, "El id de equipo no puede ser nulo");
    }

    public static EquipoId of(UUID value) {
        return new EquipoId(value);
    }
}
