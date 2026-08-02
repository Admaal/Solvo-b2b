package com.helpdesk.infrastructure.persistence.jpa;

import com.helpdesk.infrastructure.persistence.entity.OrganizacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataOrganizacionRepository extends JpaRepository<OrganizacionEntity, UUID> {
}
