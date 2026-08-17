package com.helpdesk.infrastructure.web;

import com.helpdesk.application.ports.CredencialesLogin;
import com.helpdesk.application.ports.UsuarioRepository;
import com.helpdesk.domain.exception.AutenticacionFallidaException;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.infrastructure.security.JwtService;
import com.helpdesk.infrastructure.web.dto.LoginRequest;
import com.helpdesk.infrastructure.web.dto.LoginResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UsuarioRepository usuarioRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        CredencialesLogin credenciales = usuarioRepository.buscarCredencialesPorEmail(request.email())
                .orElseThrow(AutenticacionFallidaException::new);

        if (!passwordEncoder.matches(request.password(), credenciales.passwordHash())) {
            throw new AutenticacionFallidaException();
        }

        Usuario usuario = credenciales.usuario();
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
