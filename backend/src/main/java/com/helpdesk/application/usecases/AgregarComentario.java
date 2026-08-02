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
import com.helpdesk.domain.model.UsuarioId;

import java.util.Objects;

public class AgregarComentario {

    private final ComentarioRepository comentarioRepository;
    private final TicketRepository ticketRepository;

    public AgregarComentario(ComentarioRepository comentarioRepository, TicketRepository ticketRepository) {
        this.comentarioRepository = Objects.requireNonNull(comentarioRepository);
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
    }

    public ComentarioTicket ejecutar(Comando comando) {
        Objects.requireNonNull(comando, "El comando no puede ser nulo");

        Ticket ticket = ticketRepository.buscarPorId(comando.ticketId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Ticket no encontrado: " + comando.ticketId().value()
                ));

        validarAcceso(comando.solicitante(), ticket);

        ComentarioTicket comentario = ComentarioTicket.crear(
                comando.ticketId(),
                comando.solicitante().id(),
                comando.texto()
        );

        return comentarioRepository.guardar(comentario);
    }

    private void validarAcceso(Usuario solicitante, Ticket ticket) {
        if (solicitante.rol() == Rol.CLIENTE && !ticket.clienteId().equals(solicitante.id())) {
            throw new AccesoDenegadoException("Un cliente solo puede comentar en sus propios tickets");
        }

        if (!solicitante.perteneceA(ticket.organizacionId())) {
            throw new AccesoDenegadoException("No tiene acceso a tickets de otra organización");
        }
    }

    public record Comando(Usuario solicitante, TicketId ticketId, String texto) {
        public Comando {
            Objects.requireNonNull(solicitante, "El solicitante no puede ser nulo");
            Objects.requireNonNull(ticketId, "El id del ticket no puede ser nulo");
            Objects.requireNonNull(texto, "El texto no puede ser nulo");
        }
    }
}
