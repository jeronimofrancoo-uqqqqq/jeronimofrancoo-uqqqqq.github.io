package com.monedero.repository;

import com.monedero.domain.model.TransaccionProgramada;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProgramadaRepository {
    void guardar(TransaccionProgramada programada);
    Optional<TransaccionProgramada> buscarPorId(UUID id);
    List<TransaccionProgramada> obtenerActivas();
    List<TransaccionProgramada> obtenerTodas();
}
