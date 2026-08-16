package com.sena.helpdesk.service;

import com.sena.helpdesk.dto.CambiarEstadoRequest;
import com.sena.helpdesk.dto.CrearTicketRequest;
import com.sena.helpdesk.dto.TicketResponse;
import com.sena.helpdesk.exception.AccesoDenegadoException;
import com.sena.helpdesk.exception.RecursoNoEncontradoException;
import com.sena.helpdesk.model.EstadoTicket;
import com.sena.helpdesk.model.Prioridad;
import com.sena.helpdesk.model.Rol;
import com.sena.helpdesk.model.Ticket;
import com.sena.helpdesk.model.Usuario;
import com.sena.helpdesk.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    public TicketResponse crear(Usuario creador, CrearTicketRequest request) {
        LocalDateTime ahora = LocalDateTime.now();

        Ticket ticket = Ticket.builder()
                .titulo(request.titulo())
                .descripcion(request.descripcion())
                .prioridad(request.prioridad())
                .estado(EstadoTicket.ABIERTO)
                .creadoEn(ahora)
                .slaVenceEn(calcularSla(request.prioridad(), ahora))
                .creadoPor(creador)
                .build();

        return aRespuesta(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> misTickets(Usuario usuario) {
        return ticketRepository.findByCreadoPor(usuario).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse obtener(Long id, Usuario usuario) {
        Ticket ticket = buscar(id);

        boolean esSoporteOAdmin = usuario.getRol() == Rol.SOPORTE || usuario.getRol() == Rol.ADMIN;
        boolean esDueno = ticket.getCreadoPor().getId().equals(usuario.getId());

        if (!esSoporteOAdmin && !esDueno) {
            throw new AccesoDenegadoException("No puede consultar un ticket que no le pertenece");
        }

        return aRespuesta(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listarTodos() {
        return ticketRepository.findAll().stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listarVencidos() {
        return ticketRepository.findVencidos(LocalDateTime.now(), EstadoTicket.RESUELTO).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional
    public TicketResponse cambiarEstado(Long id, CambiarEstadoRequest request) {
        Ticket ticket = buscar(id);
        ticket.setEstado(request.estado());
        return aRespuesta(ticketRepository.save(ticket));
    }

    private Ticket buscar(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un ticket con el id " + id));
    }

    /**
     * Regla de SLA del punto 5 del taller: se suma a la fecha de creación
     * una cantidad de horas según la prioridad.
     */
    private LocalDateTime calcularSla(Prioridad prioridad, LocalDateTime creadoEn) {
        long horas = switch (prioridad) {
            case ALTA -> 4;
            case MEDIA -> 24;
            case BAJA -> 72;
        };
        return creadoEn.plusHours(horas);
    }

    private TicketResponse aRespuesta(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitulo(),
                ticket.getDescripcion(),
                ticket.getPrioridad(),
                ticket.getEstado(),
                ticket.getCreadoEn(),
                ticket.getSlaVenceEn(),
                ticket.isVencido(),
                ticket.getCreadoPor().getEmail()
        );
    }
}