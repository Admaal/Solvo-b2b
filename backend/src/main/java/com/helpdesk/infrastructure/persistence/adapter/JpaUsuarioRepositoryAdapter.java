package com.helpdesk.infrastructure.persistence.adapter;

import com.helpdesk.application.ports.UsuarioRepository;
import com.helpdesk.domain.model.Usuario;
import com.helpdesk.domain.model.UsuarioId;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataUsuarioRepository;
import com.helpdesk.infrastructure.persistence.mapper.UsuarioMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class JpaUsuarioRepositoryAdapter implements UsuarioRepository {

    private final SpringDataUsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public JpaUsuarioRepositoryAdapter(
            SpringDataUsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    public Optional<Usuario> buscarPorId(UsuarioId id) {
        return usuarioRepository.findById(id.value()).map(usuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email).map(usuarioMapper::toDomain);
    }
}
