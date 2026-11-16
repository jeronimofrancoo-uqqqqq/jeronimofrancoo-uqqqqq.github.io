package com.monedero.service;

import com.monedero.domain.model.EstadoTransaccion;
import com.monedero.domain.model.TipoTransaccion;
import com.monedero.domain.model.Transaccion;
import com.monedero.domain.model.Transferencia;
import com.monedero.repository.TransaccionRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class AnalisisGastosService {
    private final TransaccionRepository transaccionRepository;

    public AnalisisGastosService(TransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    public BigDecimal calcularTotalIngresos(UUID clienteId) {
        return transaccionRepository.obtenerPorCliente(clienteId).stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.COMPLETADA)
                .filter(t -> esIngresoParaCliente(t, clienteId))
                .map(Transaccion::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularTotalGastos(UUID clienteId) {
        return transaccionRepository.obtenerPorCliente(clienteId).stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.COMPLETADA)
                .filter(t -> esGastoParaCliente(t, clienteId))
                .map(Transaccion::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<TipoTransaccion, BigDecimal> agruparGastosPorTipo(UUID clienteId) {
        return transaccionRepository.obtenerPorCliente(clienteId).stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.COMPLETADA)
                .filter(t -> esGastoParaCliente(t, clienteId))
                .collect(Collectors.groupingBy(
                        Transaccion::obtenerTipo,
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Transaccion::getMonto, BigDecimal::add)
                ));
    }

    public List<Transaccion> obtenerTopEgresos(UUID clienteId, int limite) {
        return transaccionRepository.obtenerPorCliente(clienteId).stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.COMPLETADA)
                .filter(t -> esGastoParaCliente(t, clienteId))
                .sorted(Comparator.comparing(Transaccion::getMonto).reversed())
                .limit(limite)
                .collect(Collectors.toList());
    }

    public List<Transaccion> obtenerTransaccionesSospechosas(UUID clienteId) {
        return transaccionRepository.obtenerPorCliente(clienteId).stream()
                .filter(Transaccion::isSospechosa)
                .collect(Collectors.toList());
    }

    private boolean esIngresoParaCliente(Transaccion t, UUID clienteId) {
        if (t.obtenerTipo() == TipoTransaccion.DEPOSITO) {
            return true;
        }
        if (t instanceof Transferencia tr) {
            boolean esDestino = tr.getMonederoDestino().getPropietario().getId().equals(clienteId);
            boolean esOrigen = tr.getMonederoOrigen().getPropietario().getId().equals(clienteId);
            return esDestino && !esOrigen;
        }
        return false;
    }

    private boolean esGastoParaCliente(Transaccion t, UUID clienteId) {
        if (t.obtenerTipo() == TipoTransaccion.RETIRO) {
            return true;
        }
        if (t instanceof Transferencia tr) {
            boolean esOrigen = tr.getMonederoOrigen().getPropietario().getId().equals(clienteId);
            boolean esDestino = tr.getMonederoDestino().getPropietario().getId().equals(clienteId);
            return esOrigen && !esDestino;
        }
        return false;
    }
}
