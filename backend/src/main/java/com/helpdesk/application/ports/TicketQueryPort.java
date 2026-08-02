package com.helpdesk.application.ports;

import com.helpdesk.application.common.Pagina;
import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Prioridad;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.UsuarioId;

import java.util.Optional;

public interface TicketQueryPort {

    Pagina<Ticket> buscar(
            OrganizacionId organizacionId,
            Optional<UsuarioId> clienteId,
            Optional<EstadoTicket> estado,
            Optional<Prioridad> prioridad,
            int pagina,
            int tamano,
            String ordenCampo,
            boolean ordenAscendente
    );
}
