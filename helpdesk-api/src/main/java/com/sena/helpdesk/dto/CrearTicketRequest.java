package com.sena.helpdesk.dto;

import com.sena.helpdesk.model.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos que el cliente envía al crear un ticket.
 * No incluye "estado", "creadoEn" ni "slaVenceEn": los define el servidor
 * (punto 5 del documento del taller).
 */
public record CrearTicketRequest(

        @NotBlank(message = "El título es obligatorio")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotNull(message = "La prioridad es obligatoria")
        Prioridad prioridad
) {
}