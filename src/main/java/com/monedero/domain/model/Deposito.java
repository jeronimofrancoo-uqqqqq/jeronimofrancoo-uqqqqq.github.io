package com.monedero.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Deposito extends Transaccion {
    private final Monedero monederoDestino;

    public Deposito(Monedero monederoDestino, BigDecimal monto, String descripcion) {
        super(monto, descripcion);
        if (monederoDestino == null) {
            throw new IllegalArgumentException("El monedero destino es obligatorio.");
        }
        this.monederoDestino = monederoDestino;
    }

    public Monedero getMonederoDestino() {
        return monederoDestino;
    }

    @Override
    public void ejecutar() {
        if (getEstado() != EstadoTransaccion.PENDIENTE) {
            throw new IllegalStateException("La transaccion ya fue procesada.");
        }
        monederoDestino.acreditar(getMonto());
        setEstado(EstadoTransaccion.COMPLETADA);
    }

    @Override
    public void revertir() {
        if (getEstado() != EstadoTransaccion.COMPLETADA) {
            throw new IllegalStateException("Solo se pueden revertir transacciones completadas.");
        }
        monederoDestino.debitar(getMonto());
        setEstado(EstadoTransaccion.REVERTIDA);
    }

    @Override
    public TipoTransaccion obtenerTipo() {
        return TipoTransaccion.DEPOSITO;
    }

    @Override
    public Cliente obtenerClientePrincipal() {
        return monederoDestino.getPropietario();
    }

    @Override
    public boolean involucraCliente(UUID clienteId) {
        return monederoDestino.getPropietario() != null
                && monederoDestino.getPropietario().getId().equals(clienteId);
    }
}
