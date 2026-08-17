package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.application.ports.UsuarioRepository;
import com.helpdesk.domain.exception.AsignacionNoPermitidaException;
import com.helpdesk.domain.model.Categoria;
import com.helpdesk.domain.model.EquipoId;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsignarTicketTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private AsignarTicket asignarTicket;

    private OrganizacionId organizacionId;
    private EquipoId equipoId;
    private TicketId ticketId;
    private UsuarioId solicitanteId;
    private UsuarioId agenteId;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        asignarTicket = new AsignarTicket(ticketRepository, usuarioRepository);
        organizacionId = OrganizacionId.of(UUID.randomUUID());
        equipoId = EquipoId.of(UUID.randomUUID());
        ticketId = TicketId.nuevo();
        solicitanteId = UsuarioId.of(UUID.randomUUID());
        agenteId = UsuarioId.of(UUID.randomUUID());

        ticket = Ticket.crear("Incidencia", "Detalle", organizacionId,
                UsuarioId.of(UUID.randomUUID()), new Categoria("PAGOS", Prioridad.MEDIA));
    }

    @Test
    void gestorAsignaAgenteDelMismoEquipo() {
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.of(ticket));
        when(usuarioRepository.buscarPorId(solicitanteId)).thenReturn(Optional.of(
                new Usuario(solicitanteId, "gestor@banco.test", organizacionId, Rol.GESTOR, equipoId)
        ));
        when(usuarioRepository.buscarPorId(agenteId)).thenReturn(Optional.of(
                new Usuario(agenteId, "agente@banco.test", organizacionId, Rol.GESTOR, equipoId)
        ));
        when(ticketRepository.guardar(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket resultado = asignarTicket.ejecutar(
                new AsignarTicket.Comando(ticketId, solicitanteId, agenteId)
        );

        assertEquals(agenteId, resultado.agenteAsignadoId());
        verify(ticketRepository).guardar(ticket);
    }

    @Test
    void clienteNoPuedeAsignar() {
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.of(ticket));
        when(usuarioRepository.buscarPorId(solicitanteId)).thenReturn(Optional.of(
                new Usuario(solicitanteId, "cliente@banco.test", organizacionId, Rol.CLIENTE, null)
        ));
        when(usuarioRepository.buscarPorId(agenteId)).thenReturn(Optional.of(
                new Usuario(agenteId, "agente@banco.test", organizacionId, Rol.GESTOR, equipoId)
        ));

        assertThrows(AsignacionNoPermitidaException.class, () -> asignarTicket.ejecutar(
                new AsignarTicket.Comando(ticketId, solicitanteId, agenteId)
        ));
    }

    @Test
    void gestorDeOtraOrganizacionNoPuedeAsignar() {
        OrganizacionId otraOrg = OrganizacionId.of(UUID.randomUUID());
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.of(ticket));
        when(usuarioRepository.buscarPorId(solicitanteId)).thenReturn(Optional.of(
                new Usuario(solicitanteId, "gestor@bancob.test", otraOrg, Rol.GESTOR, equipoId)
        ));
        when(usuarioRepository.buscarPorId(agenteId)).thenReturn(Optional.of(
                new Usuario(agenteId, "agente@banco.test", organizacionId, Rol.GESTOR, equipoId)
        ));

        assertThrows(AsignacionNoPermitidaException.class, () -> asignarTicket.ejecutar(
                new AsignarTicket.Comando(ticketId, solicitanteId, agenteId)
        ));
    }

    @Test
    void rechazaTicketInexistente() {
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.empty());

        assertThrows(AsignacionNoPermitidaException.class, () -> asignarTicket.ejecutar(
                new AsignarTicket.Comando(ticketId, solicitanteId, agenteId)
        ));
    }
}
