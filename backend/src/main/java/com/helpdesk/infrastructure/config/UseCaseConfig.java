package com.helpdesk.infrastructure.config;

import com.helpdesk.application.ports.CategoriaRepository;
import com.helpdesk.application.ports.TicketQueryPort;
import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.application.ports.UsuarioRepository;
import com.helpdesk.application.usecases.AgregarComentario;
import com.helpdesk.application.usecases.AsignarTicket;
import com.helpdesk.application.usecases.CalcularVencimientoSLA;
import com.helpdesk.application.usecases.CambiarEstadoTicket;
import com.helpdesk.application.usecases.CrearTicket;
import com.helpdesk.application.usecases.ListarComentarios;
import com.helpdesk.application.usecases.ListarTickets;
import com.helpdesk.application.usecases.ObtenerTicket;
import com.helpdesk.application.ports.ComentarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CrearTicket crearTicket(
            TicketRepository ticketRepository,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository
    ) {
        return new CrearTicket(ticketRepository, usuarioRepository, categoriaRepository);
    }

    @Bean
    public AsignarTicket asignarTicket(
            TicketRepository ticketRepository,
            UsuarioRepository usuarioRepository
    ) {
        return new AsignarTicket(ticketRepository, usuarioRepository);
    }

    @Bean
    public CambiarEstadoTicket cambiarEstadoTicket(TicketRepository ticketRepository) {
        return new CambiarEstadoTicket(ticketRepository);
    }

    @Bean
    public CalcularVencimientoSLA calcularVencimientoSLA(TicketRepository ticketRepository) {
        return new CalcularVencimientoSLA(ticketRepository);
    }

    @Bean
    public ListarTickets listarTickets(TicketQueryPort ticketQueryPort) {
        return new ListarTickets(ticketQueryPort);
    }

    @Bean
    public ObtenerTicket obtenerTicket(TicketRepository ticketRepository) {
        return new ObtenerTicket(ticketRepository);
    }

    @Bean
    public AgregarComentario agregarComentario(
            ComentarioRepository comentarioRepository,
            TicketRepository ticketRepository
    ) {
        return new AgregarComentario(comentarioRepository, ticketRepository);
    }

    @Bean
    public ListarComentarios listarComentarios(
            ComentarioRepository comentarioRepository,
            TicketRepository ticketRepository
    ) {
        return new ListarComentarios(comentarioRepository, ticketRepository);
    }
}
