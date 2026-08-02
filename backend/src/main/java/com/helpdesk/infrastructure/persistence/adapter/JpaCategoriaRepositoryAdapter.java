package com.helpdesk.infrastructure.persistence.adapter;

import com.helpdesk.application.ports.CategoriaRepository;
import com.helpdesk.domain.model.Categoria;
import com.helpdesk.infrastructure.persistence.jpa.SpringDataCategoriaRepository;
import com.helpdesk.infrastructure.persistence.mapper.CategoriaMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class JpaCategoriaRepositoryAdapter implements CategoriaRepository {

    private final SpringDataCategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public JpaCategoriaRepositoryAdapter(
            SpringDataCategoriaRepository categoriaRepository,
            CategoriaMapper categoriaMapper
    ) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Override
    public Optional<Categoria> buscarPorCodigo(String codigo) {
        return categoriaRepository.findById(codigo).map(categoriaMapper::toDomain);
    }
}
