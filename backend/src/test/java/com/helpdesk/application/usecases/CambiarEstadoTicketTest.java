package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.domain.exception.TransicionEstadoInvalidaException;
import com.helpdesk.domain.model.Categoria;
import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Prioridad;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketId;
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
class CambiarEstadoTicketTest {

    @Mock
    private TicketRepository ticketRepository;

    private CambiarEstadoTicket cambiarEstadoTicket;

    private TicketId ticketId;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        cambiarEstadoTicket = new CambiarEstadoTicket(ticketRepository);
        ticketId = TicketId.nuevo();
        ticket = Ticket.crear(
                "Incidencia",
                "Detalle",
                OrganizacionId.of(UUID.randomUUID()),
                UsuarioId.of(UUID.randomUUID()),
                new Categoria("ACCESOS", Prioridad.MEDIA)
        );
    }

    @Test
    void cambiaEstadoValido() {
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.guardar(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket resultado = cambiarEstadoTicket.ejecutar(
                new CambiarEstadoTicket.Comando(ticketId, EstadoTicket.EN_PROGRESO)
        );

        assertEquals(EstadoTicket.EN_PROGRESO, resultado.estado());
        verify(ticketRepository).guardar(ticket);
    }

    @Test
    void rechazaTransicionInvalida() {
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(TransicionEstadoInvalidaException.class, () -> cambiarEstadoTicket.ejecutar(
                new CambiarEstadoTicket.Comando(ticketId, EstadoTicket.CERRADO)
        ));
    }

    @Test
    void rechazaTicketInexistente() {
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.empty());

        assertThrows(TransicionEstadoInvalidaException.class, () -> cambiarEstadoTicket.ejecutar(
                new CambiarEstadoTicket.Comando(ticketId, EstadoTicket.EN_PROGRESO)
        ));
    }
}
