package com.monedero.repository.memory;

import com.monedero.domain.model.Cliente;
import com.monedero.repository.ClienteRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryClienteRepository implements ClienteRepository {
    private final Map<UUID, Cliente> datos = new LinkedHashMap<>();

    @Override
    public void guardar(Cliente cliente) {
        if (cliente != null) {
            datos.put(cliente.getId(), cliente);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Cliente> obtenerTodos() {
        return new ArrayList<>(datos.values());
    }
}
