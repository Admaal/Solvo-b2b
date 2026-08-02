package com.helpdesk.infrastructure.persistence.jpa;

import com.helpdesk.infrastructure.persistence.entity.EquipoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataEquipoRepository extends JpaRepository<EquipoEntity, UUID> {
}
