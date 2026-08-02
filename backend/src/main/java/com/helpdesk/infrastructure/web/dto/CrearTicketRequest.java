package com.helpdesk.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CrearTicketRequest(
        @NotBlank String asunto,
        @NotBlank String descripcion,
        @NotNull UUID organizacionId,
        @NotNull UUID clienteId,
        @NotBlank String codigoCategoria
) {
}
