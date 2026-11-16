package com.monedero.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class Retiro extends Transaccion {
    private final Monedero monederoOrigen;
    private final BigDecimal comision;

    public Retiro(Monedero monederoOrigen, BigDecimal monto, String descripcion) {
        this(monederoOrigen, monto, BigDecimal.ZERO, descripcion);
    }

    public Retiro(Monedero monederoOrigen, BigDecimal monto, BigDecimal comision, String descripcion) {
        super(monto, descripcion);
        if (monederoOrigen == null) {
            throw new IllegalArgumentException("El monedero origen es obligatorio.");
        }
        this.monederoOrigen = monederoOrigen;
        this.comision = comision == null ? BigDecimal.ZERO : comision.setScale(2, RoundingMode.HALF_UP);
    }

    public Monedero getMonederoOrigen() {
        return monederoOrigen;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public BigDecimal getTotalDebitado() {
        return getMonto().add(comision);
    }

    @Override
    public void ejecutar() {
        if (getEstado() != EstadoTransaccion.PENDIENTE) {
            throw new IllegalStateException("La transaccion ya fue procesada.");
        }
        monederoOrigen.debitar(getTotalDebitado());
        setEstado(EstadoTransaccion.COMPLETADA);
    }

    @Override
    public void revertir() {
        if (getEstado() != EstadoTransaccion.COMPLETADA) {
            throw new IllegalStateException("Solo se pueden revertir transacciones completadas.");
        }
        monederoOrigen.acreditar(getTotalDebitado());
        setEstado(EstadoTransaccion.REVERTIDA);
    }

    @Override
    public TipoTransaccion obtenerTipo() {
        return TipoTransaccion.RETIRO;
    }

    @Override
    public Cliente obtenerClientePrincipal() {
        return monederoOrigen.getPropietario();
    }

    @Override
    public boolean involucraCliente(UUID clienteId) {
        return monederoOrigen.getPropietario() != null
                && monederoOrigen.getPropietario().getId().equals(clienteId);
    }
}
