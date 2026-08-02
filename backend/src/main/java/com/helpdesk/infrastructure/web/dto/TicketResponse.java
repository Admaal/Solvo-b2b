package com.helpdesk.infrastructure.web.dto;

import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.Prioridad;

import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String asunto,
        String descripcion,
        EstadoTicket estado,
        Prioridad prioridad,
        String codigoCategoria,
        UUID organizacionId,
        UUID clienteId,
        UUID agenteAsignadoId,
        Instant creadoEn
) {
}
