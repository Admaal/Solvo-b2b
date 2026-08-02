package com.helpdesk.infrastructure.web;

import com.helpdesk.application.ports.UsuarioRepository;
import com.helpdesk.domain.exception.RecursoNoEncontradoException;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.infrastructure.security.JwtUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioActualProvider {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActualProvider(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal principal)) {
            throw new RecursoNoEncontradoException("Usuario autenticado no encontrado");
        }

        return usuarioRepository.buscarPorId(com.helpdesk.domain.model.UsuarioId.of(principal.usuarioId()))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado: " + principal.usuarioId()
                ));
    }
}
