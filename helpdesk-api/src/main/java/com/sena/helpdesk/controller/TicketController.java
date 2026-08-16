package com.sena.helpdesk.controller;

import com.sena.helpdesk.dto.CambiarEstadoRequest;
import com.sena.helpdesk.dto.CrearTicketRequest;
import com.sena.helpdesk.dto.TicketResponse;
import com.sena.helpdesk.model.Usuario;
import com.sena.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints 7.2 y 7.3 del taller.
 * Las rutas por rol (GET /api/tickets, PATCH .../estado, GET .../vencidos)
 * ya están protegidas en SecurityConfig; la regla "solo el dueño o
 * SOPORTE/ADMIN puede ver un ticket" se aplica en TicketService.
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // 7.2 Crea un ticket; el creador es el usuario autenticado
    @PostMapping
    public ResponseEntity<TicketResponse> crear(@Valid @RequestBody CrearTicketRequest request,
                                                @AuthenticationPrincipal Usuario usuario) {
        TicketResponse response = ticketService.crear(usuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 7.2 Lista los tickets creados por el usuario autenticado
    @GetMapping("/mios")
    public ResponseEntity<List<TicketResponse>> misTickets(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(ticketService.misTickets(usuario));
    }

    // 7.2 Consulta un ticket (dueño o SOPORTE/ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> obtener(@PathVariable Long id,
                                                  @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(ticketService.obtener(id, usuario));
    }

    // 7.3 Lista todos los tickets (SOPORTE, ADMIN)
    @GetMapping
    public ResponseEntity<List<TicketResponse>> listarTodos() {
        return ResponseEntity.ok(ticketService.listarTodos());
    }

    // 7.3 Lista los tickets que superaron su SLA (SOPORTE, ADMIN)
    @GetMapping("/vencidos")
    public ResponseEntity<List<TicketResponse>> listarVencidos() {
        return ResponseEntity.ok(ticketService.listarVencidos());
    }

    // 7.3 Cambia el estado de un ticket (SOPORTE, ADMIN)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TicketResponse> cambiarEstado(@PathVariable Long id,
                                                        @Valid @RequestBody CambiarEstadoRequest request) {
        return ResponseEntity.ok(ticketService.cambiarEstado(id, request));
    }
}