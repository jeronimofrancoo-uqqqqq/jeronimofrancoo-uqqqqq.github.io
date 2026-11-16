package com.monedero.service;

import com.monedero.domain.exception.EntidadNoEncontradaException;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.Monedero;
import com.monedero.repository.MonederoRepository;
import com.monedero.util.Validaciones;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MonederoService {
    private final MonederoRepository monederoRepository;
    private final ClienteService clienteService;

    public MonederoService(MonederoRepository monederoRepository, ClienteService clienteService) {
        this.monederoRepository = monederoRepository;
        this.clienteService = clienteService;
    }

    public Monedero crearMonedero(UUID clienteId, String nombre, BigDecimal saldoInicial) {
        Validaciones.validarTextoNoVacio(nombre, "nombre del monedero");
        Cliente propietario = clienteService.buscarPorId(clienteId);
        Monedero monedero = new Monedero(nombre.trim(), saldoInicial, propietario);
        propietario.agregarMonedero(monedero);
        monederoRepository.guardar(monedero);
        return monedero;
    }

    public Monedero buscarPorId(UUID monederoId) {
        return monederoRepository.buscarPorId(monederoId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Monedero no encontrado."));
    }

    public BigDecimal consultarSaldo(UUID monederoId) {
        return buscarPorId(monederoId).consultarSaldo();
    }

    public List<Monedero> obtenerPorCliente(UUID clienteId) {
        return monederoRepository.obtenerPorCliente(clienteId);
    }

    public List<Monedero> obtenerActivosPorCliente(UUID clienteId) {
        return monederoRepository.obtenerPorCliente(clienteId).stream()
                .filter(Monedero::isActivo)
                .collect(Collectors.toList());
    }

    public List<Monedero> listarTodos() {
        return monederoRepository.obtenerTodos();
    }

    public void acreditar(UUID monederoId, BigDecimal monto) {
        Monedero monedero = buscarPorId(monederoId);
        monedero.acreditar(monto);
        monederoRepository.guardar(monedero);
    }

    public void debitar(UUID monederoId, BigDecimal monto) {
        Monedero monedero = buscarPorId(monederoId);
        monedero.debitar(monto);
        monederoRepository.guardar(monedero);
    }
}
