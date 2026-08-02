package com.helpdesk.infrastructure.persistence.mapper;

import com.helpdesk.domain.model.Categoria;
import com.helpdesk.infrastructure.persistence.entity.CategoriaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public Categoria toDomain(CategoriaEntity entity) {
        return new Categoria(entity.getCodigo(), entity.getPrioridadPorDefecto());
    }
}
