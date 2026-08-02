package com.helpdesk.application.ports;

import com.helpdesk.domain.model.ComentarioTicket;
import com.helpdesk.domain.model.TicketId;

import java.util.List;

public interface ComentarioRepository {

    ComentarioTicket guardar(ComentarioTicket comentario);

    List<ComentarioTicket> listarPorTicket(TicketId ticketId);
}
