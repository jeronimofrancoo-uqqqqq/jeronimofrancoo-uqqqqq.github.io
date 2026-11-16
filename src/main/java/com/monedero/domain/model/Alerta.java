package com.monedero.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Alerta {
    private final UUID id;
    private final String mensaje;
    private final LocalDateTime fecha;
    private final TipoAlerta tipo;
    private final String canalSimulado;
    private boolean leida;

    public Alerta(String mensaje, TipoAlerta tipo, String canalSimulado) {
        this.id = UUID.randomUUID();
        this.mensaje = mensaje;
        this.fecha = LocalDateTime.now();
        this.tipo = tipo;
        this.canalSimulado = canalSimulado == null ? "App Local" : canalSimulado;
        this.leida = false;
    }

    public UUID getId() {
        return id;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public String getCanalSimulado() {
        return canalSimulado;
    }

    public boolean isLeida() {
        return leida;
    }

    public void marcarComoLeida() {
        this.leida = true;
    }
}
