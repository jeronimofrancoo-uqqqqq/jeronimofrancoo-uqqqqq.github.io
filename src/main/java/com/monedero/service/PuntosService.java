package com.monedero.service;

import com.monedero.domain.exception.EntidadNoEncontradaException;
import com.monedero.domain.interfaces.CalculadorPuntos;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.PuntosMovimiento;
import com.monedero.domain.model.RangoCliente;
import com.monedero.domain.model.TipoMovimientoPuntos;
import com.monedero.domain.model.TipoTransaccion;
import com.monedero.domain.model.Transaccion;
import com.monedero.repository.ClienteRepository;

import java.math.BigDecimal;
import java.util.UUID;

public class PuntosService implements CalculadorPuntos {
    private static final BigDecimal BLOQUE_PUNTOS = new BigDecimal("100");
    private final ClienteRepository clienteRepository;

    public PuntosService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // Se otorgan puntos por cada bloque completo de 100 unidades segun el tipo
    @Override
    public int calcularPuntos(Transaccion transaccion) {
        if (transaccion == null) {
            return 0;
        }
        int bloques = transaccion.getMonto().divideToIntegralValue(BLOQUE_PUNTOS).intValue();
        int tasa;
        TipoTransaccion tipo = transaccion.obtenerTipo();
        if (tipo == TipoTransaccion.DEPOSITO) {
            tasa = 1;
        } else if (tipo == TipoTransaccion.RETIRO) {
            tasa = 2;
        } else {
            tasa = 3;
        }
        int puntosBase = bloques * tasa;
        Cliente cliente = transaccion.obtenerClientePrincipal();
        if (cliente != null && puntosBase > 0) {
            double bono = cliente.getRango().getBonoPuntos();
            puntosBase += (int) Math.floor(puntosBase * bono);
        }
        return puntosBase;
    }

    public int registrarPuntosPorTransaccion(Transaccion transaccion) {
        Cliente cliente = transaccion.obtenerClientePrincipal();
        if (cliente == null) {
            return 0;
        }
        int puntos = calcularPuntos(transaccion);
        transaccion.setPuntosGenerados(puntos);
        if (puntos > 0) {
            cliente.actualizarPuntos(puntos);
            PuntosMovimiento movimiento = new PuntosMovimiento(
                    puntos,
                    "Puntos por " + transaccion.obtenerTipo().name().toLowerCase(),
                    transaccion.getId(),
                    TipoMovimientoPuntos.ACUMULACION
            );
            cliente.agregarMovimientoPuntos(movimiento);
            clienteRepository.guardar(cliente);
        }
        return puntos;
    }

    public void revertirPuntosDeTransaccion(Transaccion transaccion) {
        Cliente cliente = transaccion.obtenerClientePrincipal();
        int puntos = transaccion.getPuntosGenerados();
        if (cliente == null || puntos <= 0) {
            return;
        }
        cliente.actualizarPuntos(-puntos);
        PuntosMovimiento reverso = new PuntosMovimiento(
                -puntos,
                "Reversion de " + transaccion.obtenerTipo().name().toLowerCase(),
                transaccion.getId(),
                TipoMovimientoPuntos.REVERSO
        );
        cliente.agregarMovimientoPuntos(reverso);
        clienteRepository.guardar(cliente);
    }

    public RangoCliente calcularRango(int puntos) {
        return RangoCliente.desdePuntos(puntos);
    }

    public int consultarPuntosCliente(UUID clienteId) {
        Cliente cliente = clienteRepository.buscarPorId(clienteId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Cliente no encontrado."));
        return cliente.getPuntosAcumulados();
    }
}
