package com.helpdesk.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearComentarioRequest(
        @NotBlank @Size(min = 3, max = 2000) String texto
) {
}
