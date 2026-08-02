package com.helpdesk.infrastructure.persistence.adapter;

import com.helpdesk.application.ports.ComentarioRepository;
import com.helpdesk.domain.model.ComentarioTicket;
import com.helpdesk.domain.model.TicketId;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataComentarioRepository;
import com.helpdesk.infrastructure.persistence.mapper.ComentarioMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class JpaComentarioRepositoryAdapter implements ComentarioRepository {

    private final SpringDataComentarioRepository comentarioRepository;
    private final ComentarioMapper comentarioMapper;

    public JpaComentarioRepositoryAdapter(
            SpringDataComentarioRepository comentarioRepository,
            ComentarioMapper comentarioMapper
    ) {
        this.comentarioRepository = comentarioRepository;
        this.comentarioMapper = comentarioMapper;
    }

    @Override
    public ComentarioTicket guardar(ComentarioTicket comentario) {
        var entity = comentarioMapper.toEntity(comentario);
        var saved = comentarioRepository.save(entity);
        return comentarioMapper.toDomain(saved);
    }

    @Override
    public List<ComentarioTicket> listarPorTicket(TicketId ticketId) {
        return comentarioRepository.findByTicketIdOrderByCreadoEnAsc(ticketId.value()).stream()
                .map(comentarioMapper::toDomain)
                .toList();
    }
}
