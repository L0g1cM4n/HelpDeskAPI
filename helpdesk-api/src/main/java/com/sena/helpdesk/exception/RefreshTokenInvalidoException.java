package com.sena.helpdesk.exception;

public class RefreshTokenInvalidoException extends RuntimeException {
    public RefreshTokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
