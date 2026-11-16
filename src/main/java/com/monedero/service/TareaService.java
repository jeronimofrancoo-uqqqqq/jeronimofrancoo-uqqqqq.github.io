package com.monedero.service;

import com.monedero.domain.exception.EntidadNoEncontradaException;
import com.monedero.domain.model.Tarea;
import com.monedero.util.Validaciones;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TareaService {
    private final List<Tarea> tareas = new ArrayList<>();

    public Tarea crear(UUID clienteId, String titulo, String descripcion, LocalDate fechaLimite) {
        Validaciones.validarTextoNoVacio(titulo, "titulo");
        Tarea tarea = new Tarea(clienteId, titulo.trim(), descripcion, fechaLimite);
        tareas.add(tarea);
        return tarea;
    }

    public List<Tarea> listarPorCliente(UUID clienteId) {
        return tareas.stream()
                .filter(t -> t.getClienteId() != null && t.getClienteId().equals(clienteId))
                .collect(Collectors.toList());
    }

    public Tarea actualizar(UUID id, String titulo, String descripcion, LocalDate fechaLimite) {
        Validaciones.validarTextoNoVacio(titulo, "titulo");
        Tarea tarea = buscarPorId(id);
        tarea.setTitulo(titulo.trim());
        tarea.setDescripcion(descripcion == null ? "" : descripcion.trim());
        tarea.setFechaLimite(fechaLimite);
        return tarea;
    }

    public void cambiarEstado(UUID id, boolean completada) {
        Tarea tarea = buscarPorId(id);
        tarea.setCompletada(completada);
    }

    public void eliminar(UUID id) {
        Tarea tarea = buscarPorId(id);
        tareas.remove(tarea);
    }

    private Tarea buscarPorId(UUID id) {
        return tareas.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntidadNoEncontradaException("Tarea no encontrada."));
    }
}
