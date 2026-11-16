package com.monedero.domain.model;

import com.monedero.domain.exception.MontoInvalidoException;
import com.monedero.domain.interfaces.Reversible;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Transaccion implements Reversible {
    private final UUID id;
    private final BigDecimal monto;
    private final LocalDateTime fecha;
    private EstadoTransaccion estado;
    private final String descripcion;
    private boolean sospechosa;
    private int puntosGenerados;

    protected Transaccion(BigDecimal monto, String descripcion) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontoInvalidoException("El monto de la transaccion debe ser positivo.");
        }
        this.id = UUID.randomUUID();
        this.monto = monto.setScale(2, RoundingMode.HALF_UP);
        this.fecha = LocalDateTime.now();
        this.estado = EstadoTransaccion.PENDIENTE;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.sospechosa = false;
        this.puntosGenerados = 0;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    protected void setEstado(EstadoTransaccion estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isSospechosa() {
        return sospechosa;
    }

    public void setSospechosa(boolean sospechosa) {
        this.sospechosa = sospechosa;
    }

    public int getPuntosGenerados() {
        return puntosGenerados;
    }

    public void setPuntosGenerados(int puntosGenerados) {
        this.puntosGenerados = puntosGenerados;
    }

    public abstract void ejecutar();

    @Override
    public abstract void revertir();

    public abstract TipoTransaccion obtenerTipo();

    public abstract Cliente obtenerClientePrincipal();

    public abstract boolean involucraCliente(UUID clienteId);
}
