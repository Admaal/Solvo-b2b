package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.domain.exception.TransicionEstadoInvalidaException;
import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketId;

import java.util.Objects;

public class CambiarEstadoTicket {

    private final TicketRepository ticketRepository;

    public CambiarEstadoTicket(TicketRepository ticketRepository) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
    }

    public Ticket ejecutar(Comando comando) {
        Objects.requireNonNull(comando, "El comando no puede ser nulo");

        Ticket ticket = ticketRepository.buscarPorId(comando.ticketId())
                .orElseThrow(() -> new TransicionEstadoInvalidaException(
                        "Ticket no encontrado: " + comando.ticketId().value()
                ));

        ticket.cambiarEstado(comando.nuevoEstado());

        return ticketRepository.guardar(ticket);
    }

    public record Comando(TicketId ticketId, EstadoTicket nuevoEstado) {
        public Comando {
            Objects.requireNonNull(ticketId, "El id del ticket no puede ser nulo");
            Objects.requireNonNull(nuevoEstado, "El nuevo estado no puede ser nulo");
        }
    }
}
