package com.helpdesk.domain.model;

import java.util.Objects;

public record Categoria(String codigo, Prioridad prioridadPorDefecto) {

    public Categoria {
        Objects.requireNonNull(codigo, "El código de categoría no puede ser nulo");
        Objects.requireNonNull(prioridadPorDefecto, "La prioridad por defecto no puede ser nula");
        if (codigo.isBlank()) {
            throw new IllegalArgumentException("El código de categoría no puede estar vacío");
        }
    }
}
