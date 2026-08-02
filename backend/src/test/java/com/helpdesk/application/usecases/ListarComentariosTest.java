package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.ComentarioRepository;
import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.domain.model.ComentarioId;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarComentariosTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private TicketRepository ticketRepository;

    private ListarComentarios listarComentarios;

    private OrganizacionId organizacionId;
    private UsuarioId gestorId;
    private TicketId ticketId;

    @BeforeEach
    void setUp() {
        listarComentarios = new ListarComentarios(comentarioRepository, ticketRepository);
        organizacionId = OrganizacionId.of(UUID.randomUUID());
        gestorId = UsuarioId.of(UUID.randomUUID());
        ticketId = TicketId.of(UUID.randomUUID());
    }

    @Test
    void gestorListaComentariosDelTicket() {
        Usuario gestor = new Usuario(gestorId, "gestor@test", organizacionId, Rol.GESTOR, null);
        Ticket ticket = Ticket.reconstruir(
                ticketId,
                "Asunto",
                "Descripción",
                organizacionId,
                UsuarioId.of(UUID.randomUUID()),
                new com.helpdesk.domain.model.Categoria("ACCESOS", com.helpdesk.domain.model.Prioridad.ALTA),
                com.helpdesk.domain.model.Prioridad.ALTA,
                Instant.now(),
                EstadoTicket.EN_PROGRESO,
                gestorId
        );
        ComentarioTicket comentario = ComentarioTicket.reconstruir(
                ComentarioId.of(UUID.randomUUID()),
                ticketId,
                gestorId,
                "Seguimiento realizado",
                Instant.now()
        );

        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.of(ticket));
        when(comentarioRepository.listarPorTicket(ticketId)).thenReturn(List.of(comentario));

        List<ComentarioTicket> resultado = listarComentarios.ejecutar(
                new ListarComentarios.Comando(gestor, ticketId)
        );

        assertEquals(1, resultado.size());
        assertEquals("Seguimiento realizado", resultado.getFirst().texto());
    }
}
