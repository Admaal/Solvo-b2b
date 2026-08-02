package com.helpdesk.domain.service;

import com.helpdesk.domain.model.Prioridad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SlaCalculatorTest {

    private final Instant creadoEn = Instant.parse("2026-01-15T10:00:00Z");

    @ParameterizedTest
    @EnumSource(Prioridad.class)
    void calculaVencimientoSegunPrioridad(Prioridad prioridad) {
        Duration plazoEsperado = switch (prioridad) {
            case BAJA -> Duration.ofHours(72);
            case MEDIA -> Duration.ofHours(48);
            case ALTA -> Duration.ofHours(24);
            case CRITICA -> Duration.ofHours(4);
        };

        Instant vencimiento = SlaCalculator.calcularVencimiento(prioridad, creadoEn);
        assertEquals(creadoEn.plus(plazoEsperado), vencimiento);
    }

    @Test
    void rechazaPrioridadNula() {
        assertThrows(IllegalArgumentException.class,
                () -> SlaCalculator.calcularVencimiento(null, creadoEn));
    }

    @Test
    void rechazaFechaCreacionNula() {
        assertThrows(IllegalArgumentException.class,
                () -> SlaCalculator.calcularVencimiento(Prioridad.MEDIA, null));
    }
}
