package com.helpdesk.domain.exception;

public class AutenticacionFallidaException extends RuntimeException {

    public AutenticacionFallidaException() {
        super("Credenciales inválidas");
    }
}
