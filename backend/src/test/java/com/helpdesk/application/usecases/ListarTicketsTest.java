package com.helpdesk.application.usecases;

import com.helpdesk.application.common.Pagina;
import com.helpdesk.application.ports.TicketQueryPort;
import com.helpdesk.domain.exception.AccesoDenegadoException;
import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Prioridad;
import com.helpdesk.domain.model.Rol;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.domain.model.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarTicketsTest {

    @Mock
    private TicketQueryPort ticketQueryPort;

    private ListarTickets listarTickets;
    private OrganizacionId organizacionId;
    private Usuario cliente;
    private Usuario gestor;

    @BeforeEach
    void setUp() {
        listarTickets = new ListarTickets(ticketQueryPort);
        organizacionId = OrganizacionId.of(UUID.randomUUID());
        cliente = new Usuario(
                UsuarioId.of(UUID.randomUUID()),
                "cliente@banco.test",
                organizacionId,
                Rol.CLIENTE,
                null
        );
        gestor = new Usuario(
                UsuarioId.of(UUID.randomUUID()),
                "gestor@banco.test",
                organizacionId,
                Rol.GESTOR,
                null
        );
    }

    @Test
    void clienteSoloVeSusPropiosTickets() {
        Pagina<Ticket> paginaEsperada = new Pagina<>(List.of(), 0, 20, 0, 0);
        when(ticketQueryPort.buscar(
                eq(organizacionId),
                eq(Optional.of(cliente.id())),
                eq(Optional.empty()),
                eq(Optional.empty()),
                eq(0),
                eq(20),
                eq("creadoEn"),
                eq(false)
        )).thenReturn(paginaEsperada);

        Pagina<Ticket> resultado = listarTickets.ejecutar(new ListarTickets.Comando(
                cliente,
                organizacionId,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                0,
                20,
                "creadoEn",
                false
        ));

        assertEquals(paginaEsperada, resultado);
        verify(ticketQueryPort).buscar(
                eq(organizacionId),
                eq(Optional.of(cliente.id())),
                any(),
                any(),
                eq(0),
                eq(20),
                eq("creadoEn"),
                eq(false)
        );
    }

    @Test
    void clienteNoPuedeFiltrarPorOtroCliente() {
        assertThrows(AccesoDenegadoException.class, () -> listarTickets.ejecutar(
                new ListarTickets.Comando(
                        cliente,
                        organizacionId,
                        Optional.of(UsuarioId.of(UUID.randomUUID())),
                        Optional.empty(),
                        Optional.empty(),
                        0,
                        20,
                        "creadoEn",
                        false
                )
        ));
    }

    @Test
    void gestorPuedeListarTicketsDeLaOrganizacion() {
        Pagina<Ticket> paginaEsperada = new Pagina<>(List.of(), 0, 20, 0, 0);
        when(ticketQueryPort.buscar(any(), any(), any(), any(), any(Integer.class), any(Integer.class), any(), any(Boolean.class)))
                .thenReturn(paginaEsperada);

        Pagina<Ticket> resultado = listarTickets.ejecutar(new ListarTickets.Comando(
                gestor,
                organizacionId,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                0,
                20,
                "creadoEn",
                false
        ));

        assertEquals(paginaEsperada, resultado);
    }
}
