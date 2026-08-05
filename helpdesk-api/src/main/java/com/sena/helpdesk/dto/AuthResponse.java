package com.sena.helpdesk.dto;

/**
 * Respuesta de login y registro: entrega ambos tokens.
 * El accessToken se usa en cada petición protegida (header Authorization);
 * el refreshToken solo sirve para pedir un accessToken nuevo en /api/auth/refresh.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
