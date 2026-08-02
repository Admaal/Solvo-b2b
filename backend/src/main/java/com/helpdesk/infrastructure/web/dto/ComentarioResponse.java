package com.helpdesk.infrastructure.web.dto;

import com.helpdesk.domain.model.ComentarioTicket;

import java.time.Instant;
import java.util.UUID;

public record ComentarioResponse(
        UUID id,
        UUID ticketId,
        UUID autorId,
        String autorEmail,
        String texto,
        Instant creadoEn
) {

    public static ComentarioResponse from(ComentarioTicket comentario, String autorEmail) {
        return new ComentarioResponse(
                comentario.id().value(),
                comentario.ticketId().value(),
                comentario.autorId().value(),
                autorEmail,
                comentario.texto(),
                comentario.creadoEn()
        );
    }
}
