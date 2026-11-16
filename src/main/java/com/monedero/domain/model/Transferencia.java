package com.monedero.domain.model;

import com.monedero.domain.exception.SaldoInsuficienteException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class Transferencia extends Transaccion {
    private final Monedero monederoOrigen;
    private final Monedero monederoDestino;
    private final BigDecimal comision;

    public Transferencia(Monedero monederoOrigen, Monedero monederoDestino, BigDecimal monto, String descripcion) {
        this(monederoOrigen, monederoDestino, monto, BigDecimal.ZERO, descripcion);
    }

    public Transferencia(Monedero monederoOrigen, Monedero monederoDestino, BigDecimal monto,
                         BigDecimal comision, String descripcion) {
        super(monto, descripcion);
        if (monederoOrigen == null || monederoDestino == null) {
            throw new IllegalArgumentException("Los monederos de origen y destino son obligatorios.");
        }
        if (monederoOrigen.getId().equals(monederoDestino.getId())) {
            throw new IllegalArgumentException("El monedero origen y destino no pueden ser el mismo.");
        }
        this.monederoOrigen = monederoOrigen;
        this.monederoDestino = monederoDestino;
        this.comision = comision == null ? BigDecimal.ZERO : comision.setScale(2, RoundingMode.HALF_UP);
    }

    public Monedero getMonederoOrigen() {
        return monederoOrigen;
    }

    public Monedero getMonederoDestino() {
        return monederoDestino;
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
        if (!monederoOrigen.isActivo() || !monederoDestino.isActivo()) {
            throw new IllegalStateException("Ambos monederos deben estar activos.");
        }
        BigDecimal total = getTotalDebitado();
        if (monederoOrigen.consultarSaldo().compareTo(total) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente en " + monederoOrigen.getNombre() + ".");
        }
        monederoOrigen.debitar(total);
        monederoDestino.acreditar(getMonto());
        setEstado(EstadoTransaccion.COMPLETADA);
    }

    @Override
    public void revertir() {
        if (getEstado() != EstadoTransaccion.COMPLETADA) {
            throw new IllegalStateException("Solo se pueden revertir transacciones completadas.");
        }
        if (monederoDestino.consultarSaldo().compareTo(getMonto()) < 0) {
            throw new SaldoInsuficienteException("El monedero destino no tiene saldo suficiente para revertir.");
        }
        monederoDestino.debitar(getMonto());
        monederoOrigen.acreditar(getTotalDebitado());
        setEstado(EstadoTransaccion.REVERTIDA);
    }

    @Override
    public TipoTransaccion obtenerTipo() {
        return TipoTransaccion.TRANSFERENCIA;
    }

    @Override
    public Cliente obtenerClientePrincipal() {
        return monederoOrigen.getPropietario();
    }

    @Override
    public boolean involucraCliente(UUID clienteId) {
        boolean esOrigen = monederoOrigen.getPropietario() != null
                && monederoOrigen.getPropietario().getId().equals(clienteId);
        boolean esDestino = monederoDestino.getPropietario() != null
                && monederoDestino.getPropietario().getId().equals(clienteId);
        return esOrigen || esDestino;
    }
}
