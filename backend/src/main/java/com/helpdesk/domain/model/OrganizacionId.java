package com.helpdesk.domain.model;

import java.util.Objects;
import java.util.UUID;

public record OrganizacionId(UUID value) {

    public OrganizacionId {
        Objects.requireNonNull(value, "El id de organización no puede ser nulo");
    }

    public static OrganizacionId of(UUID value) {
        return new OrganizacionId(value);
    }
}
