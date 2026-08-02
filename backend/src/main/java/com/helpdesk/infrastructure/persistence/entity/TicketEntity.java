package com.helpdesk.infrastructure.persistence.entity;

import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.Prioridad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class TicketEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String asunto;

    @Column(nullable = false, length = 4000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTicket estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridad prioridad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_codigo", nullable = false)
    private CategoriaEntity categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private OrganizacionEntity organizacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private UsuarioEntity cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_asignado_id")
    private UsuarioEntity agenteAsignado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected TicketEntity() {
    }

    public static TicketEntity nuevo() {
        return new TicketEntity();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoTicket getEstado() {
        return estado;
    }

    public void setEstado(EstadoTicket estado) {
        this.estado = estado;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Prioridad prioridad) {
        this.prioridad = prioridad;
    }

    public CategoriaEntity getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaEntity categoria) {
        this.categoria = categoria;
    }

    public OrganizacionEntity getOrganizacion() {
        return organizacion;
    }

    public void setOrganizacion(OrganizacionEntity organizacion) {
        this.organizacion = organizacion;
    }

    public UsuarioEntity getCliente() {
        return cliente;
    }

    public void setCliente(UsuarioEntity cliente) {
        this.cliente = cliente;
    }

    public UsuarioEntity getAgenteAsignado() {
        return agenteAsignado;
    }

    public void setAgenteAsignado(UsuarioEntity agenteAsignado) {
        this.agenteAsignado = agenteAsignado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }
}
