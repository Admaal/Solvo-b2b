package com.helpdesk.infrastructure.persistence.mapper;

import com.helpdesk.domain.model.Categoria;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketId;
import com.helpdesk.domain.model.UsuarioId;
import com.helpdesk.infrastructure.persistence.entity.CategoriaEntity;
import com.helpdesk.infrastructure.persistence.entity.TicketEntity;
import com.helpdesk.infrastructure.persistence.entity.UsuarioEntity;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataCategoriaRepository;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataUsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    private final SpringDataCategoriaRepository categoriaRepository;
    private final SpringDataUsuarioRepository usuarioRepository;

    public TicketMapper(
            SpringDataCategoriaRepository categoriaRepository,
            SpringDataUsuarioRepository usuarioRepository
    ) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public TicketEntity toEntity(Ticket ticket) {
        TicketEntity entity = TicketEntity.nuevo();
        entity.setId(ticket.id().value());
        entity.setAsunto(ticket.asunto());
        entity.setDescripcion(ticket.descripcion());
        entity.setEstado(ticket.estado());
        entity.setPrioridad(ticket.prioridad());
        entity.setCreadoEn(ticket.creadoEn());

        CategoriaEntity categoria = categoriaRepository.findById(ticket.categoria().codigo())
                .orElseThrow(() -> new IllegalStateException(
                        "Categoría no encontrada: " + ticket.categoria().codigo()));
        entity.setCategoria(categoria);

        UsuarioEntity cliente = usuarioRepository.findById(ticket.clienteId().value())
                .orElseThrow(() -> new IllegalStateException("Cliente no encontrado"));
        entity.setCliente(cliente);
        entity.setOrganizacion(cliente.getOrganizacion());

        if (ticket.agenteAsignadoId() != null) {
            UsuarioEntity agente = usuarioRepository.findById(ticket.agenteAsignadoId().value())
                    .orElseThrow(() -> new IllegalStateException("Agente no encontrado"));
            entity.setAgenteAsignado(agente);
        } else {
            entity.setAgenteAsignado(null);
        }

        return entity;
    }

    public Ticket toDomain(TicketEntity entity) {
        Categoria categoria = new Categoria(
                entity.getCategoria().getCodigo(),
                entity.getCategoria().getPrioridadPorDefecto()
        );

        UsuarioId agenteId = entity.getAgenteAsignado() != null
                ? UsuarioId.of(entity.getAgenteAsignado().getId())
                : null;

        return Ticket.reconstruir(
                TicketId.of(entity.getId()),
                entity.getAsunto(),
                entity.getDescripcion(),
                OrganizacionId.of(entity.getOrganizacion().getId()),
                UsuarioId.of(entity.getCliente().getId()),
                categoria,
                entity.getPrioridad(),
                entity.getCreadoEn(),
                entity.getEstado(),
                agenteId
        );
    }
}
