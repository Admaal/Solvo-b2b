package com.helpdesk.infrastructure.persistence.entity;

import com.helpdesk.domain.model.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private OrganizacionEntity organizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id")
    private EquipoEntity equipo;

    protected UsuarioEntity() {
    }

    public UsuarioEntity(
            UUID id,
            String email,
            Rol rol,
            OrganizacionEntity organizacion,
            EquipoEntity equipo
    ) {
        this.id = id;
        this.email = email;
        this.rol = rol;
        this.organizacion = organizacion;
        this.equipo = equipo;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Rol getRol() {
        return rol;
    }

    public OrganizacionEntity getOrganizacion() {
        return organizacion;
    }

    public EquipoEntity getEquipo() {
        return equipo;
    }
}
