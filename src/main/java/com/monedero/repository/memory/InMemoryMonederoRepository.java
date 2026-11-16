package com.monedero.repository.memory;

import com.monedero.domain.model.Monedero;
import com.monedero.repository.MonederoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryMonederoRepository implements MonederoRepository {
    private final Map<UUID, Monedero> datos = new LinkedHashMap<>();

    @Override
    public void guardar(Monedero monedero) {
        if (monedero != null) {
            datos.put(monedero.getId(), monedero);
        }
    }

    @Override
    public Optional<Monedero> buscarPorId(UUID id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Monedero> obtenerPorCliente(UUID clienteId) {
        return datos.values().stream()
                .filter(m -> m.getPropietario() != null && m.getPropietario().getId().equals(clienteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Monedero> obtenerTodos() {
        return new ArrayList<>(datos.values());
    }
}
