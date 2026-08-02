package com.helpdesk.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "organizaciones")
public class OrganizacionEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    protected OrganizacionEntity() {
    }

    public OrganizacionEntity(UUID id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
