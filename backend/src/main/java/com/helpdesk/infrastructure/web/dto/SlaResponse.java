package com.helpdesk.infrastructure.web.dto;

import com.helpdesk.domain.model.Ticket;

import java.time.Instant;

public record SlaResponse(
        Instant vencimiento
) {

    public static SlaResponse from(Instant vencimiento) {
        return new SlaResponse(vencimiento);
    }
}
