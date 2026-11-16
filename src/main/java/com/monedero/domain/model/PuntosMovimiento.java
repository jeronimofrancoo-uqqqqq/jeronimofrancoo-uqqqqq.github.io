package com.monedero.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class PuntosMovimiento {
    private final UUID id;
    private final int cantidad;
    private final LocalDateTime fecha;
    private final String motivo;
    private final UUID transaccionId;
    private final TipoMovimientoPuntos tipo;

    public PuntosMovimiento(int cantidad, String motivo, UUID transaccionId, TipoMovimientoPuntos tipo) {
        this.id = UUID.randomUUID();
        this.cantidad = cantidad;
        this.fecha = LocalDateTime.now();
        this.motivo = motivo;
        this.transaccionId = transaccionId;
        this.tipo = tipo;
    }

    public UUID getId() {
        return id;
    }

    public int getCantidad() {
        return cantidad;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public UUID getTransaccionId() {
        return transaccionId;
    }

    public TipoMovimientoPuntos getTipo() {
        return tipo;
    }
}
