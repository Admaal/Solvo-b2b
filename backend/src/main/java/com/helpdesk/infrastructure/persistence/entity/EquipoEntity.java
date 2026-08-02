package com.helpdesk.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "equipos")
public class EquipoEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private OrganizacionEntity organizacion;

    protected EquipoEntity() {
    }

    public EquipoEntity(UUID id, String nombre, OrganizacionEntity organizacion) {
        this.id = id;
        this.nombre = nombre;
        this.organizacion = organizacion;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public OrganizacionEntity getOrganizacion() {
        return organizacion;
    }
}
