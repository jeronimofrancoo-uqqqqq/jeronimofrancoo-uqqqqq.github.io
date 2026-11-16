package com.monedero.domain.model;

import com.monedero.domain.exception.MontoInvalidoException;
import com.monedero.domain.exception.SaldoInsuficienteException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class Monedero {
    private final UUID id;
    private String nombre;
    private BigDecimal saldo;
    private boolean activo;
    private final Cliente propietario;

    public Monedero(String nombre, BigDecimal saldoInicial, Cliente propietario) {
        if (saldoInicial == null || saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new MontoInvalidoException("El saldo inicial no puede ser negativo.");
        }
        this.id = UUID.randomUUID();
        this.nombre = nombre;
        this.saldo = saldoInicial.setScale(2, RoundingMode.HALF_UP);
        this.activo = true;
        this.propietario = propietario;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal consultarSaldo() {
        return saldo;
    }

    public boolean isActivo() {
        return activo;
    }

    public Cliente getPropietario() {
        return propietario;
    }

    public void acreditar(BigDecimal monto) {
        if (!activo) {
            throw new IllegalStateException("El monedero esta inactivo.");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontoInvalidoException("El monto a acreditar debe ser mayor que cero.");
        }
        this.saldo = this.saldo.add(monto).setScale(2, RoundingMode.HALF_UP);
    }

    public void debitar(BigDecimal monto) {
        if (!activo) {
            throw new IllegalStateException("El monedero esta inactivo.");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontoInvalidoException("El monto a debitar debe ser mayor que cero.");
        }
        if (this.saldo.compareTo(monto) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente en el monedero " + nombre + ".");
        }
        this.saldo = this.saldo.subtract(monto).setScale(2, RoundingMode.HALF_UP);
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    @Override
    public String toString() {
        return nombre + " ($" + saldo + ")";
    }
}
