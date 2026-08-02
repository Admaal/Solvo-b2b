package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketId;
import com.helpdesk.domain.service.SlaCalculator;

import java.time.Instant;
import java.util.Objects;

public class CalcularVencimientoSLA {

    private final TicketRepository ticketRepository;

    public CalcularVencimientoSLA(TicketRepository ticketRepository) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
    }

    public Instant ejecutar(Comando comando) {
        Objects.requireNonNull(comando, "El comando no puede ser nulo");

        Ticket ticket = ticketRepository.buscarPorId(comando.ticketId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ticket no encontrado: " + comando.ticketId().value()
                ));

        return SlaCalculator.calcularVencimiento(ticket.prioridad(), ticket.creadoEn());
    }

    public record Comando(TicketId ticketId) {
        public Comando {
            Objects.requireNonNull(ticketId, "El id del ticket no puede ser nulo");
        }
    }
}
