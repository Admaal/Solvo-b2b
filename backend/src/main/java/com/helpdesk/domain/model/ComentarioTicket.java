package com.helpdesk.domain.model;

import java.time.Instant;
import java.util.Objects;

public class ComentarioTicket {

    private final ComentarioId id;
    private final TicketId ticketId;
    private final UsuarioId autorId;
    private final String texto;
    private final Instant creadoEn;

    private ComentarioTicket(
            ComentarioId id,
            TicketId ticketId,
            UsuarioId autorId,
            String texto,
            Instant creadoEn
    ) {
        this.id = id;
        this.ticketId = ticketId;
        this.autorId = autorId;
        this.texto = texto;
        this.creadoEn = creadoEn;
    }

    public static ComentarioTicket crear(TicketId ticketId, UsuarioId autorId, String texto) {
        Objects.requireNonNull(ticketId, "El ticket no puede ser nulo");
        Objects.requireNonNull(autorId, "El autor no puede ser nulo");
        Objects.requireNonNull(texto, "El texto no puede ser nulo");

        if (texto.isBlank()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }

        return new ComentarioTicket(
                ComentarioId.nuevo(),
                ticketId,
                autorId,
                texto.trim(),
                Instant.now()
        );
    }

    public static ComentarioTicket reconstruir(
            ComentarioId id,
            TicketId ticketId,
            UsuarioId autorId,
            String texto,
            Instant creadoEn
    ) {
        return new ComentarioTicket(id, ticketId, autorId, texto, creadoEn);
    }

    public ComentarioId id() {
        return id;
    }

    public TicketId ticketId() {
        return ticketId;
    }

    public UsuarioId autorId() {
        return autorId;
    }

    public String texto() {
        return texto;
    }

    public Instant creadoEn() {
        return creadoEn;
    }
}
