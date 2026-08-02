package com.helpdesk.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.infrastructure.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.Instant;

public final class SecurityErrorResponseWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private SecurityErrorResponseWriter() {
    }

    public static void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String mensaje
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI()
        );
        OBJECT_MAPPER.writeValue(response.getOutputStream(), body);
    }
}
