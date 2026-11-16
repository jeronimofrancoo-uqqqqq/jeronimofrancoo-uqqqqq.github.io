package com.monedero.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class CanjeBeneficio {
    private final UUID id;
    private final Cliente cliente;
    private final Beneficio beneficio;
    private final LocalDateTime fechaCanje;
    private EstadoCanje estado;

    public CanjeBeneficio(Cliente cliente, Beneficio beneficio) {
        this.id = UUID.randomUUID();
        this.cliente = cliente;
        this.beneficio = beneficio;
        this.fechaCanje = LocalDateTime.now();
        this.estado = EstadoCanje.PENDIENTE;
    }

    public UUID getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Beneficio getBeneficio() {
        return beneficio;
    }

    public LocalDateTime getFechaCanje() {
        return fechaCanje;
    }

    public EstadoCanje getEstado() {
        return estado;
    }

    public void aplicar() {
        if (estado == EstadoCanje.APLICADO) {
            throw new IllegalStateException("Este canje ya fue aplicado anteriormente.");
        }
        if (estado == EstadoCanje.CANCELADO) {
            throw new IllegalStateException("No se puede aplicar un canje cancelado.");
        }
        this.estado = EstadoCanje.APLICADO;
    }

    public void cancelar() {
        this.estado = EstadoCanje.CANCELADO;
    }
}
