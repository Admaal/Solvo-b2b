package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.domain.exception.AccesoDenegadoException;
import com.helpdesk.domain.exception.RecursoNoEncontradoException;
import com.helpdesk.domain.model.Categoria;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Prioridad;
import com.helpdesk.domain.model.Rol;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketId;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.domain.model.UsuarioId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerTicketTest {

    @Mock
    private TicketRepository ticketRepository;

    private ObtenerTicket obtenerTicket;
    private OrganizacionId organizacionId;
    private TicketId ticketId;
    private Ticket ticket;
    private Usuario cliente;
    private Usuario otroCliente;

    @BeforeEach
    void setUp() {
        obtenerTicket = new ObtenerTicket(ticketRepository);
        organizacionId = OrganizacionId.of(UUID.randomUUID());
        ticketId = TicketId.nuevo();
        cliente = new Usuario(UsuarioId.of(UUID.randomUUID()), "cliente@banco.test", organizacionId, Rol.CLIENTE, null);
        otroCliente = new Usuario(UsuarioId.of(UUID.randomUUID()), "otro@banco.test", organizacionId, Rol.CLIENTE, null);

        ticket = Ticket.reconstruir(
                ticketId,
                "Incidencia",
                "Detalle",
                organizacionId,
                cliente.id(),
                new Categoria("ACCESOS", Prioridad.MEDIA),
                Prioridad.MEDIA,
                Instant.now(),
                com.helpdesk.domain.model.EstadoTicket.ABIERTO,
                null
        );
    }

    @Test
    void clienteObtieneSuPropioTicket() {
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.of(ticket));

        Ticket resultado = obtenerTicket.ejecutar(new ObtenerTicket.Comando(cliente, ticketId));

        assertEquals(ticketId, resultado.id());
    }

    @Test
    void clienteNoPuedeVerTicketAjeno() {
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(AccesoDenegadoException.class, () -> obtenerTicket.ejecutar(
                new ObtenerTicket.Comando(otroCliente, ticketId)
        ));
    }

    @Test
    void ticketInexistenteLanzaExcepcion() {
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> obtenerTicket.ejecutar(
                new ObtenerTicket.Comando(cliente, ticketId)
        ));
    }
}
