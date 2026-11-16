package com.monedero.repository.memory;

import com.monedero.domain.model.TransaccionProgramada;
import com.monedero.repository.ProgramadaRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryProgramadaRepository implements ProgramadaRepository {
    private final Map<UUID, TransaccionProgramada> datos = new LinkedHashMap<>();

    @Override
    public void guardar(TransaccionProgramada programada) {
        if (programada != null) {
            datos.put(programada.getId(), programada);
        }
    }

    @Override
    public Optional<TransaccionProgramada> buscarPorId(UUID id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<TransaccionProgramada> obtenerActivas() {
        return datos.values().stream()
                .filter(TransaccionProgramada::isActiva)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransaccionProgramada> obtenerTodas() {
        return new ArrayList<>(datos.values());
    }
}
