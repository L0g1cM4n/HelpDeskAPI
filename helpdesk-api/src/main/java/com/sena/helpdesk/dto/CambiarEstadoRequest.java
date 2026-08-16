package com.sena.helpdesk.dto;

import com.sena.helpdesk.model.EstadoTicket;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de PATCH /api/tickets/{id}/estado.
 * Un valor que no exista en el enum EstadoTicket produce 400 Bad Request
 * (ver HttpMessageNotReadableException en GlobalExceptionHandler).
 */
public record CambiarEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoTicket estado
) {
}