package com.helpdesk.domain.model;

import java.util.Objects;

public record Usuario(
        UsuarioId id,
        String email,
        OrganizacionId organizacionId,
        Rol rol,
        EquipoId equipoId
) {

    public Usuario {
        Objects.requireNonNull(id, "El id de usuario no puede ser nulo");
        Objects.requireNonNull(email, "El email no puede ser nulo");
        Objects.requireNonNull(organizacionId, "La organización no puede ser nula");
        Objects.requireNonNull(rol, "El rol no puede ser nulo");
        if (email.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
    }

    public boolean perteneceA(OrganizacionId organizacion) {
        return organizacionId.equals(organizacion);
    }

    public boolean perteneceA(EquipoId equipo) {
        return equipoId != null && equipoId.equals(equipo);
    }

    public boolean puedeAsignarTickets() {
        return rol == Rol.ADMINISTRADOR || rol == Rol.GESTOR;
    }
}
