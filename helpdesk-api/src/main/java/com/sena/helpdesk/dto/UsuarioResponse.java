package com.sena.helpdesk.dto;

import com.sena.helpdesk.model.Rol;

/**
 * Representación pública de un usuario (sin password).
 */
public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        Rol rol
) {
}