package com.sena.helpdesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos que el cliente envía para registrarse.
 * No incluye "rol": todo usuario que se registra queda como USUARIO,
 * el ascenso a SOPORTE lo hace un ADMIN aparte (POST /api/admin/soporte).
 */
public record RegistroRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password
) {
}
