package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.application.ports.UsuarioRepository;
import com.helpdesk.domain.exception.AsignacionNoPermitidaException;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketId;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.domain.model.UsuarioId;

import java.util.Objects;

public class AsignarTicket {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;

    public AsignarTicket(TicketRepository ticketRepository, UsuarioRepository usuarioRepository) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
    }

    public Ticket ejecutar(Comando comando) {
        Objects.requireNonNull(comando, "El comando no puede ser nulo");

        Ticket ticket = ticketRepository.buscarPorId(comando.ticketId())
                .orElseThrow(() -> new AsignacionNoPermitidaException(
                        "Ticket no encontrado: " + comando.ticketId().value()
                ));

        Usuario solicitante = usuarioRepository.buscarPorId(comando.solicitanteId())
                .orElseThrow(() -> new AsignacionNoPermitidaException(
                        "Solicitante no encontrado: " + comando.solicitanteId().value()
                ));

        Usuario agente = usuarioRepository.buscarPorId(comando.agenteId())
                .orElseThrow(() -> new AsignacionNoPermitidaException(
                        "Agente no encontrado: " + comando.agenteId().value()
                ));

        ticket.asignarAgente(solicitante, agente);

        return ticketRepository.guardar(ticket);
    }

    public record Comando(TicketId ticketId, UsuarioId solicitanteId, UsuarioId agenteId) {
        public Comando {
            Objects.requireNonNull(ticketId, "El id del ticket no puede ser nulo");
            Objects.requireNonNull(solicitanteId, "El solicitante no puede ser nulo");
            Objects.requireNonNull(agenteId, "El agente no puede ser nulo");
        }
    }
}
