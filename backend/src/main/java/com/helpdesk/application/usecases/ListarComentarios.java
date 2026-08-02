package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.ComentarioRepository;
import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.domain.exception.AccesoDenegadoException;
import com.helpdesk.domain.exception.RecursoNoEncontradoException;
import com.helpdesk.domain.model.ComentarioTicket;
import com.helpdesk.domain.model.Rol;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketId;
import com.helpdesk.domain.model.Usuario;

import java.util.List;
import java.util.Objects;

public class ListarComentarios {

    private final ComentarioRepository comentarioRepository;
    private final TicketRepository ticketRepository;

    public ListarComentarios(ComentarioRepository comentarioRepository, TicketRepository ticketRepository) {
        this.comentarioRepository = Objects.requireNonNull(comentarioRepository);
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
    }

    public List<ComentarioTicket> ejecutar(Comando comando) {
        Objects.requireNonNull(comando, "El comando no puede ser nulo");

        Ticket ticket = ticketRepository.buscarPorId(comando.ticketId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Ticket no encontrado: " + comando.ticketId().value()
                ));

        validarAcceso(comando.solicitante(), ticket);

        return comentarioRepository.listarPorTicket(comando.ticketId());
    }

    private void validarAcceso(Usuario solicitante, Ticket ticket) {
        if (solicitante.rol() == Rol.CLIENTE && !ticket.clienteId().equals(solicitante.id())) {
            throw new AccesoDenegadoException("Un cliente solo puede ver comentarios de sus propios tickets");
        }

        if (!solicitante.perteneceA(ticket.organizacionId())) {
            throw new AccesoDenegadoException("No tiene acceso a tickets de otra organización");
        }
    }

    public record Comando(Usuario solicitante, TicketId ticketId) {
        public Comando {
            Objects.requireNonNull(solicitante, "El solicitante no puede ser nulo");
            Objects.requireNonNull(ticketId, "El id del ticket no puede ser nulo");
        }
    }
}
