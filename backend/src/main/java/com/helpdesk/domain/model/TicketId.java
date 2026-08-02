package com.helpdesk.domain.model;

import java.util.Objects;
import java.util.UUID;

public record TicketId(UUID value) {

    public TicketId {
        Objects.requireNonNull(value, "El id del ticket no puede ser nulo");
    }

    public static TicketId nuevo() {
        return new TicketId(UUID.randomUUID());
    }

    public static TicketId of(UUID value) {
        return new TicketId(value);
    }
}
