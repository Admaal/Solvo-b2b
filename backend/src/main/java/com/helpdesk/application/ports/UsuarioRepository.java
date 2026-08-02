package com.helpdesk.application.ports;

import com.helpdesk.domain.model.Usuario;
import com.helpdesk.domain.model.UsuarioId;

import java.util.Optional;

public interface UsuarioRepository {

    Optional<Usuario> buscarPorId(UsuarioId id);

    Optional<Usuario> buscarPorEmail(String email);
}
