package com.helpdesk.infrastructure.persistence.jpa;

import com.helpdesk.infrastructure.persistence.entity.ComentarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataComentarioRepository extends JpaRepository<ComentarioEntity, UUID> {

    List<ComentarioEntity> findByTicketIdOrderByCreadoEnAsc(UUID ticketId);
}
