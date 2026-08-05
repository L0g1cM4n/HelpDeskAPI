package com.sena.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(

        @NotBlank(message = "El refreshToken es obligatorio")
        String refreshToken
) {
}
