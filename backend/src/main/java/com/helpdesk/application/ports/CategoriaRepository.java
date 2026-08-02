package com.helpdesk.application.ports;

import com.helpdesk.domain.model.Categoria;

import java.util.Optional;

public interface CategoriaRepository {

    Optional<Categoria> buscarPorCodigo(String codigo);
}
