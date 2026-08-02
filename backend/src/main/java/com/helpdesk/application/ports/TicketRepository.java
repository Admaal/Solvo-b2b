package com.helpdesk.application.ports;

import com.helpdesk.domain.model.Categoria;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketId;

import java.util.Optional;

public interface TicketRepository {

    Ticket guardar(Ticket ticket);

    Optional<Ticket> buscarPorId(TicketId id);
}
