package com.helpdesk.domain.model;

import com.helpdesk.domain.exception.AsignacionNoPermitidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketAsignacionTest {

    private OrganizacionId organizacionId;
    private EquipoId equipoId;
    private Ticket ticket;
    private Usuario gestor;
    private Usuario agente;
    private Usuario cliente;

    @BeforeEach
    void setUp() {
        organizacionId = OrganizacionId.of(UUID.randomUUID());
        equipoId = EquipoId.of(UUID.randomUUID());
        Categoria categoria = new Categoria("PAGOS", Prioridad.ALTA);

        ticket = Ticket.crear("Error en transferencia", "No se completa", organizacionId,
                UsuarioId.of(UUID.randomUUID()), categoria);

        gestor = new Usuario(UsuarioId.of(UUID.randomUUID()), "gestor@banco.test", organizacionId, Rol.GESTOR, equipoId);
        agente = new Usuario(UsuarioId.of(UUID.randomUUID()), "agente@banco.test", organizacionId, Rol.GESTOR, equipoId);
        cliente = new Usuario(UsuarioId.of(UUID.randomUUID()), "cliente@banco.test", organizacionId, Rol.CLIENTE, null);
    }

    @Test
    void gestorPuedeAsignarAgenteDeLaMismaOrganizacion() {
        ticket.asignarAgente(gestor, agente);
        assertEquals(agente.id(), ticket.agenteAsignadoId());
    }

    @Test
    void clienteNoPuedeAsignarTickets() {
        assertThrows(AsignacionNoPermitidaException.class,
                () -> ticket.asignarAgente(cliente, agente));
    }

    @Test
    void noSePuedeAsignarClienteComoAgente() {
        assertThrows(AsignacionNoPermitidaException.class,
                () -> ticket.asignarAgente(gestor, cliente));
    }

    @Test
    void agenteDeOtraOrganizacionNoPuedeAsignarse() {
        Usuario agenteExterno = new Usuario(
                UsuarioId.of(UUID.randomUUID()),
                "externo@otro.test",
                OrganizacionId.of(UUID.randomUUID()),
                Rol.GESTOR,
                equipoId
        );

        assertThrows(AsignacionNoPermitidaException.class,
                () -> ticket.asignarAgente(gestor, agenteExterno));
    }
}
