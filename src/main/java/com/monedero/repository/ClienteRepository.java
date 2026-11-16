package com.monedero.repository;

import com.monedero.domain.model.Cliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {
    void guardar(Cliente cliente);
    Optional<Cliente> buscarPorId(UUID id);
    List<Cliente> obtenerTodos();
}
