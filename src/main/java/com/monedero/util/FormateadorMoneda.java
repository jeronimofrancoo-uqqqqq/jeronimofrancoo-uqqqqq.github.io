package com.monedero.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FormateadorMoneda {
    private static final NumberFormat FORMATO_MONEDA = NumberFormat.getCurrencyInstance(Locale.US);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FormateadorMoneda() {
    }

    public static String formatear(BigDecimal monto) {
        if (monto == null) {
            return "$0.00";
        }
        return FORMATO_MONEDA.format(monto);
    }

    public static String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) {
            return "-";
        }
        return fecha.format(FORMATO_FECHA);
    }
}
