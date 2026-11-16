package com.monedero.repository.memory;

import com.monedero.domain.model.Transaccion;
import com.monedero.repository.TransaccionRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryTransaccionRepository implements TransaccionRepository {
    private final Map<UUID, Transaccion> datos = new LinkedHashMap<>();

    @Override
    public void guardar(Transaccion transaccion) {
        if (transaccion != null) {
            datos.put(transaccion.getId(), transaccion);
        }
    }

    @Override
    public Optional<Transaccion> buscarPorId(UUID id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Transaccion> obtenerPorCliente(UUID clienteId) {
        return datos.values().stream()
                .filter(t -> t.involucraCliente(clienteId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaccion> obtenerTodas() {
        return new ArrayList<>(datos.values());
    }
}
