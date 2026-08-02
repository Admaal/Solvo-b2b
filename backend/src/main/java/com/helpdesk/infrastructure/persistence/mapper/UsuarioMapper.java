package com.helpdesk.infrastructure.persistence.mapper;

import com.helpdesk.domain.model.EquipoId;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.domain.model.UsuarioId;
import com.helpdesk.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toDomain(UsuarioEntity entity) {
        EquipoId equipoId = entity.getEquipo() != null
                ? EquipoId.of(entity.getEquipo().getId())
                : null;

        return new Usuario(
                UsuarioId.of(entity.getId()),
                entity.getEmail(),
                OrganizacionId.of(entity.getOrganizacion().getId()),
                entity.getRol(),
                equipoId
        );
    }
}
