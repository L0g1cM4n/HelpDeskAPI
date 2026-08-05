package com.sena.helpdesk.dto;

/**
 * Respuesta de POST /api/auth/refresh: según el documento del taller,
 * ese endpoint solo devuelve un nuevo accessToken, no un nuevo refreshToken.
 */
public record TokenResponse(
        String accessToken
) {
}
