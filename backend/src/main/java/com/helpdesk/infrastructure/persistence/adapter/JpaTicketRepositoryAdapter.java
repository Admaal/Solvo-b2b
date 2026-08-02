package com.helpdesk.infrastructure.persistence.adapter;

import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketId;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataTicketRepository;
import com.helpdesk.infrastructure.persistence.mapper.TicketMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class JpaTicketRepositoryAdapter implements TicketRepository {

    private final SpringDataTicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public JpaTicketRepositoryAdapter(
            SpringDataTicketRepository ticketRepository,
            TicketMapper ticketMapper
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    @Override
    public Ticket guardar(Ticket ticket) {
        var entity = ticketMapper.toEntity(ticket);
        var saved = ticketRepository.save(entity);
        return ticketMapper.toDomain(saved);
    }

    @Override
    public Optional<Ticket> buscarPorId(TicketId id) {
        return ticketRepository.findById(id.value()).map(ticketMapper::toDomain);
    }
}
