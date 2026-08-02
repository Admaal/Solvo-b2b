package com.helpdesk.infrastructure.persistence.entity;

import com.helpdesk.domain.model.Prioridad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "categorias")
public class CategoriaEntity {

    @Id
    @Column(length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad_por_defecto", nullable = false)
    private Prioridad prioridadPorDefecto;

    protected CategoriaEntity() {
    }

    public CategoriaEntity(String codigo, Prioridad prioridadPorDefecto) {
        this.codigo = codigo;
        this.prioridadPorDefecto = prioridadPorDefecto;
    }

    public String getCodigo() {
        return codigo;
    }

    public Prioridad getPrioridadPorDefecto() {
        return prioridadPorDefecto;
    }
}
