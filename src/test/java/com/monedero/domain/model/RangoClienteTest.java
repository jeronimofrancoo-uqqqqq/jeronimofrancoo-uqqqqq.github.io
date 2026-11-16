package com.monedero.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RangoClienteTest {

    @Test
    void debeAsignarRangosEnLimitesExactos() {
        assertEquals(RangoCliente.BRONCE, RangoCliente.desdePuntos(0));
        assertEquals(RangoCliente.BRONCE, RangoCliente.desdePuntos(500));
        assertEquals(RangoCliente.PLATA, RangoCliente.desdePuntos(501));
        assertEquals(RangoCliente.PLATA, RangoCliente.desdePuntos(1000));
        assertEquals(RangoCliente.ORO, RangoCliente.desdePuntos(1001));
        assertEquals(RangoCliente.ORO, RangoCliente.desdePuntos(5000));
        assertEquals(RangoCliente.PLATINO, RangoCliente.desdePuntos(5001));
    }
}
