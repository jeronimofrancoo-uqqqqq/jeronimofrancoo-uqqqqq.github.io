package com.monedero.domain.model;

import com.monedero.domain.exception.MontoInvalidoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransaccionProgramada {
    private final UUID id;
    private final TipoTransaccion tipo;
    private final BigDecimal monto;
    private LocalDateTime proximaEjecucion;
    private final Frecuencia frecuencia;
    private boolean activa;
    private final Monedero monederoOrigen;
    private final Monedero monederoDestino;
    private final String descripcion;
    private String ultimoResultado;

    public TransaccionProgramada(TipoTransaccion tipo, BigDecimal monto, LocalDateTime proximaEjecucion,
                                 Frecuencia frecuencia, Monedero monederoOrigen, Monedero monederoDestino,
                                 String descripcion) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontoInvalidoException("El monto programado debe ser positivo.");
        }
        if (proximaEjecucion == null || frecuencia == null || tipo == null) {
            throw new IllegalArgumentException("Tipo, fecha y frecuencia son obligatorios.");
        }
        this.id = UUID.randomUUID();
        this.tipo = tipo;
        this.monto = monto.setScale(2, RoundingMode.HALF_UP);
        this.proximaEjecucion = proximaEjecucion;
        this.frecuencia = frecuencia;
        this.activa = true;
        this.monederoOrigen = monederoOrigen;
        this.monederoDestino = monederoDestino;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.ultimoResultado = "Pendiente";
    }

    public UUID getId() {
        return id;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDateTime getProximaEjecucion() {
        return proximaEjecucion;
    }

    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    public boolean isActiva() {
        return activa;
    }

    public Monedero getMonederoOrigen() {
        return monederoOrigen;
    }

    public Monedero getMonederoDestino() {
        return monederoDestino;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUltimoResultado() {
        return ultimoResultado;
    }

    public void setUltimoResultado(String ultimoResultado) {
        this.ultimoResultado = ultimoResultado;
    }

    public LocalDateTime calcularProximaEjecucion() {
        if (frecuencia == Frecuencia.DIARIA) {
            return proximaEjecucion.plusDays(1);
        } else if (frecuencia == Frecuencia.SEMANAL) {
            return proximaEjecucion.plusWeeks(1);
        } else if (frecuencia == Frecuencia.MENSUAL) {
            return proximaEjecucion.plusMonths(1);
        }
        return proximaEjecucion;
    }

    public void marcarEjecutada() {
        this.ultimoResultado = "Ejecutada correctamente";
        if (frecuencia == Frecuencia.UNICA) {
            this.activa = false;
        } else {
            this.proximaEjecucion = calcularProximaEjecucion();
        }
    }

    public void cancelar() {
        this.activa = false;
        this.ultimoResultado = "Cancelada por el usuario";
    }
}
