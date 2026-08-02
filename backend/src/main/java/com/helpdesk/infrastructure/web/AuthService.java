package com.helpdesk.infrastructure.web;

import com.helpdesk.application.ports.UsuarioRepository;
import com.helpdesk.domain.exception.RecursoNoEncontradoException;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.infrastructure.security.JwtService;
import com.helpdesk.infrastructure.web.dto.LoginRequest;
import com.helpdesk.infrastructure.web.dto.LoginResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.buscarPorEmail(request.email())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado: " + request.email()
                ));

        String token = jwtService.generarToken(
                usuario.id().value(),
                usuario.email(),
                usuario.rol()
        );

        return new LoginResponse(
                token,
                usuario.email(),
                usuario.rol().name(),
                usuario.id().value(),
                usuario.organizacionId().value()
        );
    }
}
