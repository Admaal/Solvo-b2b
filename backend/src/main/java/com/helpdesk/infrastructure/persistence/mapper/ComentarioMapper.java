package com.helpdesk.infrastructure.persistence.mapper;

import com.helpdesk.domain.model.ComentarioId;
import com.helpdesk.domain.model.ComentarioTicket;
import com.helpdesk.domain.model.TicketId;
import com.helpdesk.domain.model.UsuarioId;
import com.helpdesk.infrastructure.persistence.entity.ComentarioEntity;
import com.helpdesk.infrastructure.persistence.entity.TicketEntity;
import com.helpdesk.infrastructure.persistence.entity.UsuarioEntity;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataTicketRepository;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataUsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class ComentarioMapper {

    private final SpringDataTicketRepository ticketRepository;
    private final SpringDataUsuarioRepository usuarioRepository;

    public ComentarioMapper(
            SpringDataTicketRepository ticketRepository,
            SpringDataUsuarioRepository usuarioRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ComentarioEntity toEntity(ComentarioTicket comentario) {
        ComentarioEntity entity = ComentarioEntity.nuevo();
        entity.setId(comentario.id().value());
        entity.setTicket(ticketRepository.getReferenceById(comentario.ticketId().value()));
        entity.setAutor(usuarioRepository.getReferenceById(comentario.autorId().value()));
        entity.setTexto(comentario.texto());
        entity.setCreadoEn(comentario.creadoEn());
        return entity;
    }

    public ComentarioTicket toDomain(ComentarioEntity entity) {
        TicketEntity ticket = entity.getTicket();
        UsuarioEntity autor = entity.getAutor();
        return ComentarioTicket.reconstruir(
                ComentarioId.of(entity.getId()),
                TicketId.of(ticket.getId()),
                UsuarioId.of(autor.getId()),
                entity.getTexto(),
                entity.getCreadoEn()
        );
    }
}
