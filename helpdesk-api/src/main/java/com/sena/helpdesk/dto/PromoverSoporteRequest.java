package com.sena.helpdesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de POST /api/admin/soporte: email del usuario a ascender a SOPORTE.
 */
public record PromoverSoporteRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        String email
) {
}