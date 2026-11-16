package com.monedero.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Cliente {
    private final UUID id;
    private String nombre;
    private String correo;
    private String telefono;
    private String pin;
    private int puntosAcumulados;
    private RangoCliente rango;
    private final List<Monedero> monederos;
    private final List<PuntosMovimiento> historialPuntos;
    private final List<Alerta> alertas;
    private final List<CanjeBeneficio> canjes;

    public Cliente(String nombre, String correo, String telefono) {
        this(nombre, correo, telefono, "1234");
    }

    public Cliente(String nombre, String correo, String telefono, String pin) {
        this.id = UUID.randomUUID();
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.pin = (pin == null || pin.trim().isEmpty()) ? "1234" : pin.trim();
        this.puntosAcumulados = 0;
        this.rango = RangoCliente.BRONCE;
        this.monederos = new ArrayList<>();
        this.historialPuntos = new ArrayList<>();
        this.alertas = new ArrayList<>();
        this.canjes = new ArrayList<>();
    }

    public boolean validarPin(String intento) {
        return intento != null && this.pin.equals(intento.trim());
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getPuntosAcumulados() {
        return puntosAcumulados;
    }

    public RangoCliente getRango() {
        return rango;
    }

    public void agregarMonedero(Monedero monedero) {
        if (monedero != null && !monederos.contains(monedero)) {
            monederos.add(monedero);
        }
    }

    public List<Monedero> obtenerMonederos() {
        return Collections.unmodifiableList(monederos);
    }

    public void agregarMovimientoPuntos(PuntosMovimiento movimiento) {
        if (movimiento != null) {
            historialPuntos.add(movimiento);
        }
    }

    public List<PuntosMovimiento> getHistorialPuntos() {
        return Collections.unmodifiableList(historialPuntos);
    }

    public void agregarAlerta(Alerta alerta) {
        if (alerta != null) {
            alertas.add(alerta);
        }
    }

    public List<Alerta> getAlertas() {
        return Collections.unmodifiableList(alertas);
    }

    public void agregarCanje(CanjeBeneficio canje) {
        if (canje != null) {
            canjes.add(canje);
        }
    }

    public List<CanjeBeneficio> getCanjes() {
        return Collections.unmodifiableList(canjes);
    }

    // Evita que los puntos queden negativos tras una reversion
    public void actualizarPuntos(int delta) {
        int nuevoTotal = this.puntosAcumulados + delta;
        if (nuevoTotal < 0) {
            nuevoTotal = 0;
        }
        this.puntosAcumulados = nuevoTotal;
        actualizarRango();
    }

    public void actualizarRango() {
        this.rango = RangoCliente.desdePuntos(this.puntosAcumulados);
    }

    @Override
    public String toString() {
        return nombre + " (" + rango.name() + " - " + puntosAcumulados + " pts)";
    }
}
