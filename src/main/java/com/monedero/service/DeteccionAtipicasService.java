package com.monedero.service;

import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.EstadoTransaccion;
import com.monedero.domain.model.TipoAlerta;
import com.monedero.domain.model.Transaccion;
import com.monedero.repository.TransaccionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeteccionAtipicasService {
    private static final int MINIMO_HISTORIAL = 3;
    private final TransaccionRepository transaccionRepository;
    private final NotificacionService notificacionService;

    public DeteccionAtipicasService(TransaccionRepository transaccionRepository,
                                    NotificacionService notificacionService) {
        this.transaccionRepository = transaccionRepository;
        this.notificacionService = notificacionService;
    }

    public List<Double> obtenerMontosHistoricos(UUID clienteId, UUID excluirTransaccionId) {
        return transaccionRepository.obtenerPorCliente(clienteId).stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.COMPLETADA)
                .filter(t -> excluirTransaccionId == null || !t.getId().equals(excluirTransaccionId))
                .map(t -> t.getMonto().doubleValue())
                .collect(Collectors.toList());
    }

    public double calcularPromedio(List<Double> montos) {
        if (montos == null || montos.isEmpty()) {
            return 0.0;
        }
        return montos.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    // Desviacion estandar poblacional en O(n)
    public double calcularDesviacionEstandar(List<Double> montos, double promedio) {
        if (montos == null || montos.isEmpty()) {
            return 0.0;
        }
        double sumaCuadrados = montos.stream()
                .mapToDouble(m -> Math.pow(m - promedio, 2))
                .sum();
        return Math.sqrt(sumaCuadrados / montos.size());
    }

    public double calcularUmbral(List<Double> montos) {
        double promedio = calcularPromedio(montos);
        double desviacion = calcularDesviacionEstandar(montos, promedio);
        return promedio + (2.0 * desviacion);
    }

    public boolean esAtipica(UUID clienteId, Transaccion transaccion) {
        if (clienteId == null || transaccion == null) {
            return false;
        }
        List<Double> montos = obtenerMontosHistoricos(clienteId, transaccion.getId());
        if (montos.size() < MINIMO_HISTORIAL) {
            return false;
        }
        double umbral = calcularUmbral(montos);
        return transaccion.getMonto().doubleValue() > umbral;
    }

    public boolean analizarYAlertar(Transaccion transaccion) {
        Cliente cliente = transaccion.obtenerClientePrincipal();
        if (cliente == null) {
            return false;
        }
        List<Double> montos = obtenerMontosHistoricos(cliente.getId(), transaccion.getId());
        if (montos.size() < MINIMO_HISTORIAL) {
            return false;
        }
        double umbral = calcularUmbral(montos);
        if (transaccion.getMonto().doubleValue() > umbral) {
            transaccion.setSospechosa(true);
            transaccionRepository.guardar(transaccion);
            String mensaje = String.format(
                    "Transaccion atipica detectada por $%s (supera umbral habitual de $%.2f).",
                    transaccion.getMonto(), umbral
            );
            notificacionService.notificarCliente(cliente, mensaje, TipoAlerta.TRANSACCION_ATIPICA, "App Local");
            return true;
        }
        return false;
    }
}
