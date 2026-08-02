package com.helpdesk.infrastructure.web.mapper;

import com.helpdesk.application.common.Pagina;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.infrastructure.web.dto.PaginaResponse;
import com.helpdesk.infrastructure.web.dto.TicketResponse;
import org.springframework.stereotype.Component;

@Component
public class TicketDtoMapper {

    public TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.id().value(),
                ticket.asunto(),
                ticket.descripcion(),
                ticket.estado(),
                ticket.prioridad(),
                ticket.categoria().codigo(),
                ticket.organizacionId().value(),
                ticket.clienteId().value(),
                ticket.agenteAsignadoId() != null ? ticket.agenteAsignadoId().value() : null,
                ticket.creadoEn()
        );
    }

    public PaginaResponse<TicketResponse> toPaginaResponse(Pagina<Ticket> pagina) {
        return new PaginaResponse<>(
                pagina.elementos().stream().map(this::toResponse).toList(),
                pagina.numeroPagina(),
                pagina.tamanoPagina(),
                pagina.totalElementos(),
                pagina.totalPaginas()
        );
    }
}
