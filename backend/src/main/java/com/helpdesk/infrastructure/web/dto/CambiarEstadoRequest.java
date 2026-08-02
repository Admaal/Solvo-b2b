package com.helpdesk.infrastructure.web.dto;

import com.helpdesk.domain.model.EstadoTicket;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(
        @NotNull EstadoTicket estado
) {
}
