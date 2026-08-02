package com.helpdesk.infrastructure.web;

import com.helpdesk.domain.exception.AccesoDenegadoException;
import com.helpdesk.domain.exception.AsignacionNoPermitidaException;
import com.helpdesk.domain.exception.CreacionTicketInvalidaException;
import com.helpdesk.domain.exception.RecursoNoEncontradoException;
import com.helpdesk.domain.exception.TransicionEstadoInvalidaException;
import com.helpdesk.infrastructure.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(
            RecursoNoEncontradoException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler({
            AccesoDenegadoException.class,
            AccessDeniedException.class
    })
    public ResponseEntity<ErrorResponse> accesoDenegado(
            Exception ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(error(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ErrorResponse> noAutenticado(
            InsufficientAuthenticationException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(error(HttpStatus.UNAUTHORIZED, "Autenticación requerida", request.getRequestURI()));
    }

    @ExceptionHandler({
            CreacionTicketInvalidaException.class,
            TransicionEstadoInvalidaException.class,
            AsignacionNoPermitidaException.class,
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ErrorResponse> solicitudInvalida(
            Exception ex,
            HttpServletRequest request
    ) {
        String mensaje = ex instanceof MethodArgumentNotValidException manve
                ? manve.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Solicitud inválida")
                : ex.getMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error(HttpStatus.BAD_REQUEST, mensaje, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorInterno(
            Exception ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request.getRequestURI()));
    }

    private ErrorResponse error(HttpStatus status, String mensaje, String path) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), mensaje, path);
    }
}
