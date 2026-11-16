package com.monedero.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Beneficio {
    private final UUID id;
    private final String nombre;
    private final String descripcion;
    private final int costoPuntos;
    private final TipoBeneficio tipo;
    private final BigDecimal valor;
    private boolean activo;

    public Beneficio(String nombre, String descripcion, int costoPuntos, TipoBeneficio tipo, BigDecimal valor) {
        this.id = UUID.randomUUID();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoPuntos = costoPuntos;
        this.tipo = tipo;
        this.valor = valor;
        this.activo = true;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCostoPuntos() {
        return costoPuntos;
    }

    public TipoBeneficio getTipo() {
        return tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public boolean estaDisponible() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre + " (" + costoPuntos + " pts)";
    }
}
