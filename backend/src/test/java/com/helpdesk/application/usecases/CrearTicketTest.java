package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.CategoriaRepository;
import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.application.ports.UsuarioRepository;
import com.helpdesk.domain.exception.CreacionTicketInvalidaException;
import com.helpdesk.domain.model.Categoria;
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
import org.mockito.ArgumentCaptor;
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
class CrearTicketTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private CrearTicket crearTicket;

    private OrganizacionId organizacionId;
    private UsuarioId clienteId;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        crearTicket = new CrearTicket(ticketRepository, usuarioRepository, categoriaRepository);
        organizacionId = OrganizacionId.of(UUID.randomUUID());
        clienteId = UsuarioId.of(UUID.randomUUID());
        categoria = new Categoria("ACCESOS", Prioridad.ALTA);
    }

    @Test
    void creaTicketConPrioridadPorDefectoDeLaCategoria() {
        when(categoriaRepository.buscarPorCodigo("ACCESOS")).thenReturn(Optional.of(categoria));
        when(usuarioRepository.buscarPorId(clienteId)).thenReturn(Optional.of(
                new Usuario(clienteId, "cliente@banco.test", organizacionId, Rol.CLIENTE, null)
        ));
        when(ticketRepository.guardar(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket ticket = crearTicket.ejecutar(new CrearTicket.Comando(
                "No puedo entrar",
                "Error 403 en portal",
                organizacionId,
                clienteId,
                "ACCESOS"
        ));

        assertEquals(Prioridad.ALTA, ticket.prioridad());
        assertEquals(EstadoTicket.ABIERTO, ticket.estado());
        assertEquals(organizacionId, ticket.organizacionId());
        assertEquals(clienteId, ticket.clienteId());

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).guardar(captor.capture());
        assertEquals("No puedo entrar", captor.getValue().asunto());
    }

    @Test
    void rechazaClienteDeOtraOrganizacion() {
        when(categoriaRepository.buscarPorCodigo("ACCESOS")).thenReturn(Optional.of(categoria));
        when(usuarioRepository.buscarPorId(clienteId)).thenReturn(Optional.of(
                new Usuario(clienteId, "cliente@banco.test", OrganizacionId.of(UUID.randomUUID()), Rol.CLIENTE, null)
        ));

        assertThrows(CreacionTicketInvalidaException.class, () -> crearTicket.ejecutar(
                new CrearTicket.Comando("Asunto", "Descripción", organizacionId, clienteId, "ACCESOS")
        ));
    }

    @Test
    void rechazaUsuarioQueNoEsCliente() {
        when(categoriaRepository.buscarPorCodigo("ACCESOS")).thenReturn(Optional.of(categoria));
        when(usuarioRepository.buscarPorId(clienteId)).thenReturn(Optional.of(
                new Usuario(clienteId, "gestor@banco.test", organizacionId, Rol.GESTOR, null)
        ));

        assertThrows(CreacionTicketInvalidaException.class, () -> crearTicket.ejecutar(
                new CrearTicket.Comando("Asunto", "Descripción", organizacionId, clienteId, "ACCESOS")
        ));
    }

    @Test
    void rechazaCategoriaInexistente() {
        when(categoriaRepository.buscarPorCodigo("DESCONOCIDA")).thenReturn(Optional.empty());

        assertThrows(CreacionTicketInvalidaException.class, () -> crearTicket.ejecutar(
                new CrearTicket.Comando("Asunto", "Descripción", organizacionId, clienteId, "DESCONOCIDA")
        ));
    }
}
