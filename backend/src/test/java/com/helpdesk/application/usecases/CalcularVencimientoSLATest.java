package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.domain.model.Categoria;
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

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalcularVencimientoSLATest {

    @Mock
    private TicketRepository ticketRepository;

    private CalcularVencimientoSLA calcularVencimientoSLA;

    private TicketId ticketId;
    private Ticket ticket;
    private Instant creadoEn;

    @BeforeEach
    void setUp() {
        calcularVencimientoSLA = new CalcularVencimientoSLA(ticketRepository);
        ticketId = TicketId.nuevo();
        creadoEn = Instant.parse("2026-02-01T09:00:00Z");

        ticket = Ticket.reconstruir(
                ticketId,
                "Incidencia crítica",
                "Sistema caído",
                OrganizacionId.of(UUID.randomUUID()),
                UsuarioId.of(UUID.randomUUID()),
                new Categoria("PAGOS", Prioridad.CRITICA),
                Prioridad.CRITICA,
                creadoEn,
                com.helpdesk.domain.model.EstadoTicket.ABIERTO,
                null
        );
    }

    @Test
    void calculaVencimientoSegunPrioridadDelTicket() {
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.of(ticket));

        Instant vencimiento = calcularVencimientoSLA.ejecutar(
                new CalcularVencimientoSLA.Comando(ticketId)
        );

        assertEquals(creadoEn.plus(Duration.ofHours(4)), vencimiento);
    }

    @Test
    void rechazaTicketInexistente() {
        when(ticketRepository.buscarPorId(ticketId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> calcularVencimientoSLA.ejecutar(
                new CalcularVencimientoSLA.Comando(ticketId)
        ));
    }
}
