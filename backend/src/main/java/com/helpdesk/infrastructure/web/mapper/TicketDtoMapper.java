package com.helpdesk.infrastructure.web.mapper;

import com.helpdesk.application.common.Pagina;
import com.helpdesk.application.ports.UsuarioRepository;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.infrastructure.web.dto.PaginaResponse;
import com.helpdesk.infrastructure.web.dto.TicketResponse;
import org.springframework.stereotype.Component;

@Component
public class TicketDtoMapper {

    private final UsuarioRepository usuarioRepository;

    public TicketDtoMapper(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public TicketResponse toResponse(Ticket ticket) {
        String clienteEmail = usuarioRepository.buscarPorId(ticket.clienteId())
                .map(Usuario::email)
                .orElse(null);
        String agenteEmail = ticket.agenteAsignadoId() == null
                ? null
                : usuarioRepository.buscarPorId(ticket.agenteAsignadoId())
                .map(Usuario::email)
                .orElse(null);

        return new TicketResponse(
                ticket.id().value(),
                ticket.asunto(),
                ticket.descripcion(),
                ticket.estado(),
                ticket.prioridad(),
                ticket.categoria().codigo(),
                ticket.organizacionId().value(),
                ticket.clienteId().value(),
                clienteEmail,
                ticket.agenteAsignadoId() != null ? ticket.agenteAsignadoId().value() : null,
                agenteEmail,
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
