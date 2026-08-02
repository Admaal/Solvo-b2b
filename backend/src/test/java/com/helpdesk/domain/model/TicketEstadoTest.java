package com.helpdesk.domain.model;

import com.helpdesk.domain.exception.TransicionEstadoInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketEstadoTest {

    private OrganizacionId organizacionId;
    private UsuarioId clienteId;
    private Categoria categoria;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        organizacionId = OrganizacionId.of(UUID.randomUUID());
        clienteId = UsuarioId.of(UUID.randomUUID());
        categoria = new Categoria("ACCESOS", Prioridad.MEDIA);
        ticket = Ticket.crear("No puedo acceder", "El portal devuelve 403", organizacionId, clienteId, categoria);
    }

    @Test
    void nuevoTicketEmpiezaEnAbierto() {
        assertEquals(EstadoTicket.ABIERTO, ticket.estado());
    }

    @Test
    void transicionValidaAbiertoAEnProgreso() {
        ticket.cambiarEstado(EstadoTicket.EN_PROGRESO);
        assertEquals(EstadoTicket.EN_PROGRESO, ticket.estado());
    }

    @Test
    void transicionValidaEnProgresoAResuelto() {
        ticket.cambiarEstado(EstadoTicket.EN_PROGRESO);
        ticket.cambiarEstado(EstadoTicket.RESUELTO);
        assertEquals(EstadoTicket.RESUELTO, ticket.estado());
    }

    @Test
    void transicionValidaResueltoACerrado() {
        ticket.cambiarEstado(EstadoTicket.EN_PROGRESO);
        ticket.cambiarEstado(EstadoTicket.RESUELTO);
        ticket.cambiarEstado(EstadoTicket.CERRADO);
        assertEquals(EstadoTicket.CERRADO, ticket.estado());
    }

    @Test
    void transicionValidaReaperturaResueltoAEnProgreso() {
        ticket.cambiarEstado(EstadoTicket.EN_PROGRESO);
        ticket.cambiarEstado(EstadoTicket.RESUELTO);
        ticket.cambiarEstado(EstadoTicket.EN_PROGRESO);
        assertEquals(EstadoTicket.EN_PROGRESO, ticket.estado());
    }

    @Test
    void transicionInvalidaAbiertoACerrado() {
        TransicionEstadoInvalidaException ex = assertThrows(
                TransicionEstadoInvalidaException.class,
                () -> ticket.cambiarEstado(EstadoTicket.CERRADO)
        );
        assertEquals("Transición inválida de ABIERTO a CERRADO", ex.getMessage());
        assertEquals(EstadoTicket.ABIERTO, ticket.estado());
    }

    @Test
    void transicionInvalidaAbiertoAResuelto() {
        assertThrows(TransicionEstadoInvalidaException.class,
                () -> ticket.cambiarEstado(EstadoTicket.RESUELTO));
    }

    @Test
    void desdeCerradoNoPermiteNingunaTransicion() {
        ticket.cambiarEstado(EstadoTicket.EN_PROGRESO);
        ticket.cambiarEstado(EstadoTicket.RESUELTO);
        ticket.cambiarEstado(EstadoTicket.CERRADO);

        assertThrows(TransicionEstadoInvalidaException.class,
                () -> ticket.cambiarEstado(EstadoTicket.CERRADO));
        assertEquals(EstadoTicket.CERRADO, ticket.estado());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoTicket.class, mode = EnumSource.Mode.EXCLUDE, names = "CERRADO")
    void desdeCerradoNoSePuedeTransicionar(EstadoTicket destino) {
        ticket.cambiarEstado(EstadoTicket.EN_PROGRESO);
        ticket.cambiarEstado(EstadoTicket.RESUELTO);
        ticket.cambiarEstado(EstadoTicket.CERRADO);

        assertThrows(TransicionEstadoInvalidaException.class,
                () -> ticket.cambiarEstado(destino));
        assertEquals(EstadoTicket.CERRADO, ticket.estado());
    }

    @Test
    void puedeTransicionarAReflejaReglas() {
        assertTrue(ticket.puedeTransicionarA(EstadoTicket.EN_PROGRESO));
        assertFalse(ticket.puedeTransicionarA(EstadoTicket.CERRADO));
        assertFalse(ticket.puedeTransicionarA(EstadoTicket.RESUELTO));
    }

    @Test
    void cambiarEstadoRechazaNulo() {
        assertThrows(NullPointerException.class, () -> ticket.cambiarEstado(null));
    }
}
