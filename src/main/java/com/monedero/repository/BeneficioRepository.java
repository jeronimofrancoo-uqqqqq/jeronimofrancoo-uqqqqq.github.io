package com.monedero.repository;

import com.monedero.domain.model.Beneficio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeneficioRepository {
    void guardar(Beneficio beneficio);
    List<Beneficio> obtenerActivos();
    Optional<Beneficio> buscarPorId(UUID id);
}
