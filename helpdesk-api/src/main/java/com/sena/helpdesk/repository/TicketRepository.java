package com.sena.helpdesk.repository;

import com.sena.helpdesk.model.EstadoTicket;
import com.sena.helpdesk.model.Ticket;
import com.sena.helpdesk.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreadoPor(Usuario usuario);

    // Tickets vencidos: superaron su SLA y no están resueltos
    @Query("SELECT t FROM Ticket t WHERE t.slaVenceEn < :ahora AND t.estado <> :estadoResuelto")
    List<Ticket> findVencidos(@Param("ahora") LocalDateTime ahora,
        @Param("estadoResuelto") EstadoTicket estadoResuelto);
}
