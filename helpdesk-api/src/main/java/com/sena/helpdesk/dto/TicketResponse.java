package com.sena.helpdesk.dto;

import com.sena.helpdesk.model.EstadoTicket;
import com.sena.helpdesk.model.Prioridad;

import java.time.LocalDateTime;

/**
 * Representación de un ticket que se devuelve al cliente.
 * Incluye el campo "vencido" (la fecha actual superó slaVenceEn y el
 * estado no es RESUELTO), tal como pide el punto 5 del taller.
 */
public record TicketResponse(
        Long id,
        String titulo,
        String descripcion,
        Prioridad prioridad,
        EstadoTicket estado,
        LocalDateTime creadoEn,
        LocalDateTime slaVenceEn,
        boolean vencido,
        String creadoPor
) {
}