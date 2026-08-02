package com.helpdesk.infrastructure.web;

import com.helpdesk.application.ports.UsuarioRepository;
import com.helpdesk.application.usecases.AgregarComentario;
import com.helpdesk.application.usecases.ListarComentarios;
import com.helpdesk.domain.model.ComentarioTicket;
import com.helpdesk.domain.model.TicketId;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.domain.model.UsuarioId;
import com.helpdesk.infrastructure.web.dto.ComentarioResponse;
import com.helpdesk.infrastructure.web.dto.CrearComentarioRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comentarios")
@Tag(name = "Comentarios", description = "Comentarios en tickets")
public class ComentarioController {

    private final AgregarComentario agregarComentario;
    private final ListarComentarios listarComentarios;
    private final UsuarioActualProvider usuarioActualProvider;
    private final UsuarioRepository usuarioRepository;

    public ComentarioController(
            AgregarComentario agregarComentario,
            ListarComentarios listarComentarios,
            UsuarioActualProvider usuarioActualProvider,
            UsuarioRepository usuarioRepository
    ) {
        this.agregarComentario = agregarComentario;
        this.listarComentarios = listarComentarios;
        this.usuarioActualProvider = usuarioActualProvider;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @Operation(summary = "Listar comentarios del ticket")
    public List<ComentarioResponse> listar(@PathVariable UUID ticketId) {
        Usuario solicitante = usuarioActualProvider.obtenerUsuarioActual();
        return listarComentarios.ejecutar(new ListarComentarios.Comando(
                solicitante,
                TicketId.of(ticketId)
        )).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @Operation(summary = "Añadir comentario al ticket")
    public ResponseEntity<ComentarioResponse> crear(
            @PathVariable UUID ticketId,
            @Valid @RequestBody CrearComentarioRequest request
    ) {
        Usuario solicitante = usuarioActualProvider.obtenerUsuarioActual();
        ComentarioTicket comentario = agregarComentario.ejecutar(new AgregarComentario.Comando(
                solicitante,
                TicketId.of(ticketId),
                request.texto()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(comentario));
    }

    private ComentarioResponse toResponse(ComentarioTicket comentario) {
        String email = usuarioRepository.buscarPorId(comentario.autorId())
                .map(Usuario::email)
                .orElse(comentario.autorId().value().toString());
        return ComentarioResponse.from(comentario, email);
    }
}
