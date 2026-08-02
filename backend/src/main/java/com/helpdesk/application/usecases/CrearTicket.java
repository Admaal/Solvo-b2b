package com.helpdesk.application.usecases;

import com.helpdesk.application.ports.CategoriaRepository;
import com.helpdesk.application.ports.TicketRepository;
import com.helpdesk.application.ports.UsuarioRepository;
import com.helpdesk.domain.exception.CreacionTicketInvalidaException;
import com.helpdesk.domain.model.Categoria;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Rol;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.domain.model.UsuarioId;

import java.util.Objects;
import java.util.UUID;

public class CrearTicket {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    public CrearTicket(
            TicketRepository ticketRepository,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository
    ) {
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
        this.categoriaRepository = Objects.requireNonNull(categoriaRepository);
    }

    public Ticket ejecutar(Comando comando) {
        Objects.requireNonNull(comando, "El comando no puede ser nulo");

        Categoria categoria = categoriaRepository.buscarPorCodigo(comando.codigoCategoria())
                .orElseThrow(() -> new CreacionTicketInvalidaException(
                        "Categoría no encontrada: " + comando.codigoCategoria()
                ));

        Usuario cliente = usuarioRepository.buscarPorId(comando.clienteId())
                .orElseThrow(() -> new CreacionTicketInvalidaException(
                        "Cliente no encontrado: " + comando.clienteId().value()
                ));

        if (cliente.rol() != Rol.CLIENTE) {
            throw new CreacionTicketInvalidaException("Solo un usuario con rol CLIENTE puede crear tickets");
        }

        if (!cliente.perteneceA(comando.organizacionId())) {
            throw new CreacionTicketInvalidaException("El cliente no pertenece a la organización indicada");
        }

        Ticket ticket = Ticket.crear(
                comando.asunto(),
                comando.descripcion(),
                comando.organizacionId(),
                comando.clienteId(),
                categoria
        );

        return ticketRepository.guardar(ticket);
    }

    public record Comando(
            String asunto,
            String descripcion,
            OrganizacionId organizacionId,
            UsuarioId clienteId,
            String codigoCategoria
    ) {
        public Comando {
            Objects.requireNonNull(asunto, "El asunto no puede ser nulo");
            Objects.requireNonNull(descripcion, "La descripción no puede ser nula");
            Objects.requireNonNull(organizacionId, "La organización no puede ser nula");
            Objects.requireNonNull(clienteId, "El cliente no puede ser nulo");
            Objects.requireNonNull(codigoCategoria, "El código de categoría no puede ser nulo");
        }
    }
}
