package com.sena.helpdesk.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String titulo;

    @NotBlank
    @Column(nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoTicket estado = EstadoTicket.ABIERTO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    // Calculado por el servidor a partir de prioridad + creadoEn, nunca lo envía el cliente
    @Column(nullable = false)
    private LocalDateTime slaVenceEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_id", nullable = false)
    private Usuario creadoPor;

    /**
     * Un ticket está vencido si la fecha actual superó slaVenceEn
     * y el estado no es RESUELTO.
     */
    @Transient
    public boolean isVencido() {
        return estado != EstadoTicket.RESUELTO && LocalDateTime.now().isAfter(slaVenceEn);
    }
}
