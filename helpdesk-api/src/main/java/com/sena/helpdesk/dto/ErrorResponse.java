package com.sena.helpdesk.dto;

import java.time.LocalDateTime;

/**
 * Formato uniforme para todas las respuestas de error de la API
 * (400, 401, 403, 404, 409).
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta
) {
}
