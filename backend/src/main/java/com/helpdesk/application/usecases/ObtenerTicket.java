package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.domain.exception.AccesoDenegadoException;
import com.helpdesk.domain.exception.RecursoNoEncontradoException;
import com.helpdesk.domain.model.Rol;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketId;
import com.helpdesk.domain.model.Usuario;

import java.util.Objects;

public class ObtenerTicket {

    private final TicketRepository ticketRepository;

    public ObtenerTicket(TicketRepository ticketRepository) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
    }

    public Ticket ejecutar(Comando comando) {
        Objects.requireNonNull(comando, "El comando no puede ser nulo");

        Ticket ticket = ticketRepository.buscarPorId(comando.ticketId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Ticket no encontrado: " + comando.ticketId().value()
                ));

        Usuario solicitante = comando.solicitante();

        if (solicitante.rol() == Rol.CLIENTE && !ticket.clienteId().equals(solicitante.id())) {
            throw new AccesoDenegadoException("Un cliente solo puede ver sus propios tickets");
        }

        if (!solicitante.perteneceA(ticket.organizacionId())) {
            throw new AccesoDenegadoException("No tiene acceso a tickets de otra organización");
        }

        return ticket;
    }

    public record Comando(Usuario solicitante, TicketId ticketId) {
        public Comando {
            Objects.requireNonNull(solicitante, "El solicitante no puede ser nulo");
            Objects.requireNonNull(ticketId, "El id del ticket no puede ser nulo");
        }
    }
}
