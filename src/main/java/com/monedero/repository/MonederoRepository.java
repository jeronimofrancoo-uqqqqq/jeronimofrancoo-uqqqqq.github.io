package com.monedero.repository;

import com.monedero.domain.model.Monedero;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonederoRepository {
    void guardar(Monedero monedero);
    Optional<Monedero> buscarPorId(UUID id);
    List<Monedero> obtenerPorCliente(UUID clienteId);
    List<Monedero> obtenerTodos();
}
