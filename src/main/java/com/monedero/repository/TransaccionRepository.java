package com.monedero.repository;

import com.monedero.domain.model.Transaccion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransaccionRepository {
    void guardar(Transaccion transaccion);
    Optional<Transaccion> buscarPorId(UUID id);
    List<Transaccion> obtenerPorCliente(UUID clienteId);
    List<Transaccion> obtenerTodas();
}
