package com.helpdesk.domain.service;

import com.helpdesk.domain.model.Prioridad;

import java.time.Duration;
import java.time.Instant;

public final class SlaCalculator {

    private SlaCalculator() {
    }

    public static Instant calcularVencimiento(Prioridad prioridad, Instant creadoEn) {
        if (prioridad == null) {
            throw new IllegalArgumentException("La prioridad no puede ser nula");
        }
        if (creadoEn == null) {
            throw new IllegalArgumentException("La fecha de creación no puede ser nula");
        }

        Duration plazo = switch (prioridad) {
            case BAJA -> Duration.ofHours(72);
            case MEDIA -> Duration.ofHours(48);
            case ALTA -> Duration.ofHours(24);
            case CRITICA -> Duration.ofHours(4);
        };

        return creadoEn.plus(plazo);
    }
}
