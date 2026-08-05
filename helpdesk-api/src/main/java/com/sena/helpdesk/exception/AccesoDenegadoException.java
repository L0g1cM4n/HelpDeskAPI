package com.sena.helpdesk.exception;

/**
 * Para casos de 403 que no vienen del chequeo de rol de Spring Security
 * (@PreAuthorize), sino de una regla de negocio manual — por ejemplo,
 * un USUARIO que intenta ver un ticket que no le pertenece.
 */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
