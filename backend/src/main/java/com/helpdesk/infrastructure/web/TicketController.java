package com.helpdesk.infrastructure.web;

import com.helpdesk.application.usecases.AsignarTicket;
import com.helpdesk.application.usecases.CalcularVencimientoSLA;
import com.helpdesk.application.usecases.CambiarEstadoTicket;
import com.helpdesk.application.usecases.CrearTicket;
import com.helpdesk.application.usecases.ListarTickets;
import com.helpdesk.application.usecases.ObtenerTicket;
import com.helpdesk.domain.model.EstadoTicket;
import com.helpdesk.domain.model.OrganizacionId;
import com.helpdesk.domain.model.Prioridad;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketId;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.domain.model.UsuarioId;
import com.helpdesk.infrastructure.web.dto.AsignarTicketRequest;
import com.helpdesk.infrastructure.web.dto.CambiarEstadoRequest;
import com.helpdesk.infrastructure.web.dto.CrearTicketRequest;
import com.helpdesk.infrastructure.web.dto.PaginaResponse;
import com.helpdesk.infrastructure.web.dto.SlaResponse;
import com.helpdesk.infrastructure.web.dto.TicketResponse;
import com.helpdesk.infrastructure.web.mapper.TicketDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "Gestión de tickets")
public class TicketController {

    private final CrearTicket crearTicket;
    private final ListarTickets listarTickets;
    private final ObtenerTicket obtenerTicket;
    private final AsignarTicket asignarTicket;
    private final CambiarEstadoTicket cambiarEstadoTicket;
    private final CalcularVencimientoSLA calcularVencimientoSLA;
    private final UsuarioActualProvider usuarioActualProvider;
    private final TicketDtoMapper ticketDtoMapper;

    public TicketController(
            CrearTicket crearTicket,
            ListarTickets listarTickets,
            ObtenerTicket obtenerTicket,
            AsignarTicket asignarTicket,
            CambiarEstadoTicket cambiarEstadoTicket,
            CalcularVencimientoSLA calcularVencimientoSLA,
            UsuarioActualProvider usuarioActualProvider,
            TicketDtoMapper ticketDtoMapper
    ) {
        this.crearTicket = crearTicket;
        this.listarTickets = listarTickets;
        this.obtenerTicket = obtenerTicket;
        this.asignarTicket = asignarTicket;
        this.cambiarEstadoTicket = cambiarEstadoTicket;
        this.calcularVencimientoSLA = calcularVencimientoSLA;
        this.usuarioActualProvider = usuarioActualProvider;
        this.ticketDtoMapper = ticketDtoMapper;
    }

    @GetMapping
    @Operation(summary = "Listar tickets con paginación y filtros")
    public PaginaResponse<TicketResponse> listar(
            @RequestParam UUID organizacionId,
            @RequestParam(required = false) UUID clienteId,
            @RequestParam(required = false) EstadoTicket estado,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "creadoEn") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Usuario solicitante = usuarioActualProvider.obtenerUsuarioActual();

        var resultado = listarTickets.ejecutar(new ListarTickets.Comando(
                solicitante,
                OrganizacionId.of(organizacionId),
                Optional.ofNullable(clienteId).map(UsuarioId::of),
                Optional.ofNullable(estado),
                Optional.ofNullable(prioridad),
                page,
                size,
                sort,
                !"desc".equalsIgnoreCase(direction)
        ));

        return ticketDtoMapper.toPaginaResponse(resultado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ticket por id")
    public TicketResponse obtener(@PathVariable UUID id) {
        Usuario solicitante = usuarioActualProvider.obtenerUsuarioActual();
        Ticket ticket = obtenerTicket.ejecutar(new ObtenerTicket.Comando(
                solicitante,
                TicketId.of(id)
        ));
        return ticketDtoMapper.toResponse(ticket);
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Crear ticket")
    public ResponseEntity<TicketResponse> crear(@Valid @RequestBody CrearTicketRequest request) {
        Ticket ticket = crearTicket.ejecutar(new CrearTicket.Comando(
                request.asunto(),
                request.descripcion(),
                OrganizacionId.of(request.organizacionId()),
                UsuarioId.of(request.clienteId()),
                request.codigoCategoria()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketDtoMapper.toResponse(ticket));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMINISTRADOR')")
    @Operation(summary = "Cambiar estado del ticket")
    public TicketResponse cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambiarEstadoRequest request
    ) {
        Ticket ticket = cambiarEstadoTicket.ejecutar(new CambiarEstadoTicket.Comando(
                TicketId.of(id),
                request.estado()
        ));
        return ticketDtoMapper.toResponse(ticket);
    }

    @PatchMapping("/{id}/asignacion")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMINISTRADOR')")
    @Operation(summary = "Asignar agente al ticket")
    public TicketResponse asignar(
            @PathVariable UUID id,
            @Valid @RequestBody AsignarTicketRequest request
    ) {
        Usuario solicitante = usuarioActualProvider.obtenerUsuarioActual();
        Ticket ticket = asignarTicket.ejecutar(new AsignarTicket.Comando(
                TicketId.of(id),
                solicitante.id(),
                UsuarioId.of(request.agenteId())
        ));
        return ticketDtoMapper.toResponse(ticket);
    }

    @GetMapping("/{id}/sla")
    @Operation(summary = "Calcular vencimiento SLA del ticket")
    public SlaResponse calcularSla(@PathVariable UUID id) {
        Usuario solicitante = usuarioActualProvider.obtenerUsuarioActual();
        obtenerTicket.ejecutar(new ObtenerTicket.Comando(solicitante, TicketId.of(id)));

        var vencimiento = calcularVencimientoSLA.ejecutar(
                new CalcularVencimientoSLA.Comando(TicketId.of(id))
        );
        return SlaResponse.from(vencimiento);
    }
}
