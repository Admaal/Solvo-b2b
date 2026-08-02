package com.helpdesk.domain.model;

import com.helpdesk.domain.exception.AsignacionNoPermitidaException;
import com.helpdesk.domain.exception.TransicionEstadoInvalidaException;

import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Ticket {

    private static final Map<EstadoTicket, Set<EstadoTicket>> TRANSICIONES_VALIDAS = new EnumMap<>(EstadoTicket.class);

    static {
        TRANSICIONES_VALIDAS.put(EstadoTicket.ABIERTO, EnumSet.of(EstadoTicket.EN_PROGRESO));
        TRANSICIONES_VALIDAS.put(EstadoTicket.EN_PROGRESO, EnumSet.of(EstadoTicket.RESUELTO));
        TRANSICIONES_VALIDAS.put(EstadoTicket.RESUELTO, EnumSet.of(EstadoTicket.CERRADO, EstadoTicket.EN_PROGRESO));
        TRANSICIONES_VALIDAS.put(EstadoTicket.CERRADO, EnumSet.noneOf(EstadoTicket.class));
    }

    private final TicketId id;
    private final String asunto;
    private final String descripcion;
    private final OrganizacionId organizacionId;
    private final UsuarioId clienteId;
    private final Categoria categoria;
    private final Prioridad prioridad;
    private final Instant creadoEn;
    private EstadoTicket estado;
    private UsuarioId agenteAsignadoId;

    private Ticket(
            TicketId id,
            String asunto,
            String descripcion,
            OrganizacionId organizacionId,
            UsuarioId clienteId,
            Categoria categoria,
            Prioridad prioridad,
            Instant creadoEn,
            EstadoTicket estado,
            UsuarioId agenteAsignadoId
    ) {
        this.id = id;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.organizacionId = organizacionId;
        this.clienteId = clienteId;
        this.categoria = categoria;
        this.prioridad = prioridad;
        this.creadoEn = creadoEn;
        this.estado = estado;
        this.agenteAsignadoId = agenteAsignadoId;
    }

    public static Ticket crear(
            String asunto,
            String descripcion,
            OrganizacionId organizacionId,
            UsuarioId clienteId,
            Categoria categoria
    ) {
        Objects.requireNonNull(asunto, "El asunto no puede ser nulo");
        Objects.requireNonNull(descripcion, "La descripción no puede ser nula");
        Objects.requireNonNull(organizacionId, "La organización no puede ser nula");
        Objects.requireNonNull(clienteId, "El cliente no puede ser nulo");
        Objects.requireNonNull(categoria, "La categoría no puede ser nula");

        if (asunto.isBlank()) {
            throw new IllegalArgumentException("El asunto no puede estar vacío");
        }
        if (descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }

        return new Ticket(
                TicketId.nuevo(),
                asunto,
                descripcion,
                organizacionId,
                clienteId,
                categoria,
                categoria.prioridadPorDefecto(),
                Instant.now(),
                EstadoTicket.ABIERTO,
                null
        );
    }

    public static Ticket reconstruir(
            TicketId id,
            String asunto,
            String descripcion,
            OrganizacionId organizacionId,
            UsuarioId clienteId,
            Categoria categoria,
            Prioridad prioridad,
            Instant creadoEn,
            EstadoTicket estado,
            UsuarioId agenteAsignadoId
    ) {
        return new Ticket(
                id,
                asunto,
                descripcion,
                organizacionId,
                clienteId,
                categoria,
                prioridad,
                creadoEn,
                estado,
                agenteAsignadoId
        );
    }

    public void cambiarEstado(EstadoTicket nuevoEstado) {
        Objects.requireNonNull(nuevoEstado, "El nuevo estado no puede ser nulo");

        if (!puedeTransicionarA(nuevoEstado)) {
            throw new TransicionEstadoInvalidaException(
                    "Transición inválida de " + estado + " a " + nuevoEstado
            );
        }

        this.estado = nuevoEstado;
    }

    public boolean puedeTransicionarA(EstadoTicket nuevoEstado) {
        return TRANSICIONES_VALIDAS.getOrDefault(estado, EnumSet.noneOf(EstadoTicket.class))
                .contains(nuevoEstado);
    }

    public void asignarAgente(Usuario solicitante, Usuario agente) {
        Objects.requireNonNull(solicitante, "El solicitante no puede ser nulo");
        Objects.requireNonNull(agente, "El agente no puede ser nulo");

        if (!solicitante.puedeAsignarTickets()) {
            throw new AsignacionNoPermitidaException("Solo administradores y gestores pueden asignar tickets");
        }

        if (!agente.perteneceA(organizacionId)) {
            throw new AsignacionNoPermitidaException("El agente no pertenece a la organización del ticket");
        }

        if (agente.rol() == Rol.CLIENTE) {
            throw new AsignacionNoPermitidaException("Un cliente no puede ser asignado como agente");
        }

        this.agenteAsignadoId = agente.id();
    }

    public TicketId id() {
        return id;
    }

    public String asunto() {
        return asunto;
    }

    public String descripcion() {
        return descripcion;
    }

    public OrganizacionId organizacionId() {
        return organizacionId;
    }

    public UsuarioId clienteId() {
        return clienteId;
    }

    public Categoria categoria() {
        return categoria;
    }

    public Prioridad prioridad() {
        return prioridad;
    }

    public Instant creadoEn() {
        return creadoEn;
    }

    public EstadoTicket estado() {
        return estado;
    }

    public UsuarioId agenteAsignadoId() {
        return agenteAsignadoId;
    }
}
