package com.helpdesk.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AsignarTicketRequest(
        @NotNull UUID agenteId
) {
}
