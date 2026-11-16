package com.monedero.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class Tarea {
    private final UUID id;
    private final UUID clienteId;
    private String titulo;
    private String descripcion;
    private LocalDate fechaLimite;
    private boolean completada;

    public Tarea(UUID clienteId, String titulo, String descripcion, LocalDate fechaLimite) {
        this.id = UUID.randomUUID();
        this.clienteId = clienteId;
        this.titulo = titulo;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.fechaLimite = fechaLimite;
        this.completada = false;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }
}
