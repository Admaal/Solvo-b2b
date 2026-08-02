package com.helpdesk.infrastructure.persistence.adapter;

import com.helpdesk.application.common.Pagina;
import com.helpdesk.application.ports.TicketQueryPort;
import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Prioridad;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.UsuarioId;
import com.helpdesk.infrastructure.persistence.entity.TicketEntity;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataTicketRepository;
import com.helpdesk.infrastructure.persistence.mapper.TicketMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class JpaTicketQueryAdapter implements TicketQueryPort {

    private final SpringDataTicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public JpaTicketQueryAdapter(SpringDataTicketRepository ticketRepository, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    @Override
    public Pagina<Ticket> buscar(
            OrganizacionId organizacionId,
            Optional<UsuarioId> clienteId,
            Optional<EstadoTicket> estado,
            Optional<Prioridad> prioridad,
            int pagina,
            int tamano,
            String ordenCampo,
            boolean ordenAscendente
    ) {
        Specification<TicketEntity> spec = Specification.where(porOrganizacion(organizacionId.value()))
                .and(clienteId.map(id -> porCliente(id.value())).orElse(null))
                .and(estado.map(this::porEstado).orElse(null))
                .and(prioridad.map(this::porPrioridad).orElse(null));

        Sort sort = Sort.by(ordenAscendente ? Sort.Direction.ASC : Sort.Direction.DESC, mapearCampo(ordenCampo));
        PageRequest pageRequest = PageRequest.of(pagina, tamano, sort);
        Page<TicketEntity> paginaResultado = ticketRepository.findAll(spec, pageRequest);

        return new Pagina<>(
                paginaResultado.getContent().stream().map(ticketMapper::toDomain).toList(),
                paginaResultado.getNumber(),
                paginaResultado.getSize(),
                paginaResultado.getTotalElements(),
                paginaResultado.getTotalPages()
        );
    }

    private Specification<TicketEntity> porOrganizacion(UUID organizacionId) {
        return (root, query, cb) -> cb.equal(root.get("organizacion").get("id"), organizacionId);
    }

    private Specification<TicketEntity> porCliente(UUID clienteId) {
        return (root, query, cb) -> cb.equal(root.get("cliente").get("id"), clienteId);
    }

    private Specification<TicketEntity> porEstado(EstadoTicket estado) {
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    private Specification<TicketEntity> porPrioridad(Prioridad prioridad) {
        return (root, query, cb) -> cb.equal(root.get("prioridad"), prioridad);
    }

    private String mapearCampo(String ordenCampo) {
        return switch (ordenCampo) {
            case "prioridad" -> "prioridad";
            case "estado" -> "estado";
            case "asunto" -> "asunto";
            default -> "creadoEn";
        };
    }
}
