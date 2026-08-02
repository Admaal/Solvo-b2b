package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.ComentarioRepository;
import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.domain.exception.AccesoDenegadoException;
import com.helpdesk.domain.model.ComentarioTicket;
import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.OrganizacionId;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgregarComentarioTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private TicketRepository ticketRepository;

    private AgregarComentario agregarComentario;

    private OrganizacionId organizacionId;
    private UsuarioId clienteId;
    private UsuarioId otroClienteId;
    private TicketId ticketId;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        agregarComentario = new AgregarComentario(comentarioRepository, ticketRepository);
        organizacionId = OrganizacionId.of(UUID.randomUUID());
        clienteId = UsuarioId.of(UUID.randomUUID());
        otroClienteId = UsuarioId.of(UUID.randomUUID());
        ticketId = TicketId.of(UUID.randomUUID());
        ticket = Ticket.reconstruir(
                ticketId,
                "Asunto",
                "Descripción",
                organizacionId,
                clienteId,
                new com.helpdesk.domain.model.Categoria("ACCESOS", com.helpdesk.domain.model.Prioridad.ALTA),
                com.helpdesk.domain.model.Prioridad.ALTA,
                java.time.Instant.now(),
                EstadoTicket.ABIERTO,
                null
        );
    }

    @Test
    void clientePuedeComentarSuTicket() {
        Usuario cliente = new Usuario(clienteId, "cliente@test", organizacionId, Rol.CLIENTE, null);
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.of(ticket));
        when(comentarioRepository.guardar(any(ComentarioTicket.class))).thenAnswer(inv -> inv.getArgument(0));

        ComentarioTicket comentario = agregarComentario.ejecutar(
                new AgregarComentario.Comando(cliente, ticketId, "Más detalle del problema")
        );

        assertEquals("Más detalle del problema", comentario.texto());
        verify(comentarioRepository).guardar(any(ComentarioTicket.class));
    }

    @Test
    void clienteNoPuedeComentarTicketAjeno() {
        Usuario otroCliente = new Usuario(otroClienteId, "otro@test", organizacionId, Rol.CLIENTE, null);
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(AccesoDenegadoException.class, () -> agregarComentario.ejecutar(
                new AgregarComentario.Comando(otroCliente, ticketId, "Intento no autorizado")
        ));
    }
}
