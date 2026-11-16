package com.monedero.domain.model;

public enum RangoCliente {
    BRONCE(0, 500, 0.0, 0.0),
    PLATA(501, 1000, 0.10, 0.0),
    ORO(1001, 5000, 0.25, 0.05),
    PLATINO(5001, Integer.MAX_VALUE, 0.50, 0.10);

    private final int minPuntos;
    private final int maxPuntos;
    private final double descuentoComision;
    private final double bonoPuntos;

    RangoCliente(int minPuntos, int maxPuntos, double descuentoComision, double bonoPuntos) {
        this.minPuntos = minPuntos;
        this.maxPuntos = maxPuntos;
        this.descuentoComision = descuentoComision;
        this.bonoPuntos = bonoPuntos;
    }

    public int getMinPuntos() {
        return minPuntos;
    }

    public int getMaxPuntos() {
        return maxPuntos;
    }

    public double getDescuentoComision() {
        return descuentoComision;
    }

    public double getBonoPuntos() {
        return bonoPuntos;
    }

    public static RangoCliente desdePuntos(int puntos) {
        if (puntos <= 500) {
            return BRONCE;
        } else if (puntos <= 1000) {
            return PLATA;
        } else if (puntos <= 5000) {
            return ORO;
        } else {
            return PLATINO;
        }
    }
}
