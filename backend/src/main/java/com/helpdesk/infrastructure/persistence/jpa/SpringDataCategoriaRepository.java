package com.helpdesk.infrastructure.persistence.jpa;

import com.helpdesk.infrastructure.persistence.entity.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCategoriaRepository extends JpaRepository<CategoriaEntity, String> {
}
