package com.helpdesk.application.common;

import java.util.List;
import java.util.Objects;

public record Pagina<T>(
        List<T> elementos,
        int numeroPagina,
        int tamanoPagina,
        long totalElementos,
        int totalPaginas
) {

    public Pagina {
        Objects.requireNonNull(elementos, "Los elementos no pueden ser nulos");
    }
}
