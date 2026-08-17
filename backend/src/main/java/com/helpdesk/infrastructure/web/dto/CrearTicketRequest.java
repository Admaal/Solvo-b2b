package com.helpdesk.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearTicketRequest(
        @NotBlank @Size(min = 5, max = 200) String asunto,
        @NotBlank @Size(min = 10, max = 4000) String descripcion,
        @NotBlank String codigoCategoria
) {
}
