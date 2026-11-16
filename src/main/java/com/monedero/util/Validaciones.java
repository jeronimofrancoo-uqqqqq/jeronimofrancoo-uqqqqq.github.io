package com.monedero.util;

import com.monedero.domain.exception.MontoInvalidoException;

import java.math.BigDecimal;

public final class Validaciones {

    private Validaciones() {
    }

    public static void validarTextoNoVacio(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " es obligatorio.");
        }
    }

    public static BigDecimal parsearMontoPositivo(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new MontoInvalidoException("Debe ingresar un monto.");
        }
        try {
            BigDecimal monto = new BigDecimal(texto.trim().replace(",", "."));
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new MontoInvalidoException("El monto debe ser mayor que cero.");
            }
            return monto;
        } catch (NumberFormatException e) {
            throw new MontoInvalidoException("El formato del monto no es valido.", e);
        }
    }
}
