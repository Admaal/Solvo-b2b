package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.TicketQueryPort;
import com.helpdesk.application.common.Pagina;
import com.helpdesk.domain.exception.AccesoDenegadoException;
import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Prioridad;
import com.helpdesk.domain.model.Rol;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.domain.model.UsuarioId;

import java.util.Objects;
import java.util.Optional;

public class ListarTickets {

    private final TicketQueryPort ticketQueryPort;

    public ListarTickets(TicketQueryPort ticketQueryPort) {
        this.ticketQueryPort = Objects.requireNonNull(ticketQueryPort);
    }

    public Pagina<Ticket> ejecutar(Comando comando) {
        Objects.requireNonNull(comando, "El comando no puede ser nulo");

        Usuario solicitante = comando.solicitante();
        Optional<UsuarioId> clienteFiltro = comando.clienteId();

        if (solicitante.rol() == Rol.CLIENTE) {
            if (clienteFiltro.isPresent() && !clienteFiltro.get().equals(solicitante.id())) {
                throw new AccesoDenegadoException("Un cliente solo puede ver sus propios tickets");
            }
            clienteFiltro = Optional.of(solicitante.id());
        }

        if (!solicitante.perteneceA(comando.organizacionId())) {
            throw new AccesoDenegadoException("No tiene acceso a tickets de otra organización");
        }

        return ticketQueryPort.buscar(
                comando.organizacionId(),
                clienteFiltro,
                comando.estado(),
                comando.prioridad(),
                comando.pagina(),
                comando.tamano(),
                comando.ordenCampo(),
                comando.ordenAscendente()
        );
    }

    public record Comando(
            Usuario solicitante,
            OrganizacionId organizacionId,
            Optional<UsuarioId> clienteId,
            Optional<EstadoTicket> estado,
            Optional<Prioridad> prioridad,
            int pagina,
            int tamano,
            String ordenCampo,
            boolean ordenAscendente
    ) {
        public Comando {
            Objects.requireNonNull(solicitante, "El solicitante no puede ser nulo");
            Objects.requireNonNull(organizacionId, "La organización no puede ser nula");
            Objects.requireNonNull(clienteId, "El filtro de cliente no puede ser nulo");
            Objects.requireNonNull(estado, "El filtro de estado no puede ser nulo");
            Objects.requireNonNull(prioridad, "El filtro de prioridad no puede ser nulo");
            if (pagina < 0) {
                throw new IllegalArgumentException("La página no puede ser negativa");
            }
            if (tamano < 1 || tamano > 100) {
                throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 100");
            }
        }
    }
}
