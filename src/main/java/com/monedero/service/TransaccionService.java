package com.monedero.service;

import com.monedero.domain.exception.EntidadNoEncontradaException;
import com.monedero.domain.model.CanjeBeneficio;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.Deposito;
import com.monedero.domain.model.EstadoCanje;
import com.monedero.domain.model.Monedero;
import com.monedero.domain.model.Retiro;
import com.monedero.domain.model.TipoAlerta;
import com.monedero.domain.model.TipoBeneficio;
import com.monedero.domain.model.TipoTransaccion;
import com.monedero.domain.model.Transferencia;
import com.monedero.domain.model.Transaccion;
import com.monedero.repository.TransaccionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TransaccionService {
    private static final BigDecimal UMBRAL_SALDO_BAJO = new BigDecimal("50.00");
    private static final BigDecimal TASA_COMISION_RETIRO = new BigDecimal("0.01");
    private static final BigDecimal TASA_COMISION_TRANSFERENCIA = new BigDecimal("0.02");

    private final TransaccionRepository transaccionRepository;
    private final MonederoService monederoService;
    private final PuntosService puntosService;
    private final DeteccionAtipicasService deteccionAtipicasService;
    private final NotificacionService notificacionService;

    public TransaccionService(TransaccionRepository transaccionRepository,
                              MonederoService monederoService,
                              PuntosService puntosService,
                              DeteccionAtipicasService deteccionAtipicasService,
                              NotificacionService notificacionService) {
        this.transaccionRepository = transaccionRepository;
        this.monederoService = monederoService;
        this.puntosService = puntosService;
        this.deteccionAtipicasService = deteccionAtipicasService;
        this.notificacionService = notificacionService;
    }

    public Deposito depositar(UUID monederoId, BigDecimal monto, String descripcion) {
        Monedero destino = monederoService.buscarPorId(monederoId);
        Deposito deposito = new Deposito(destino, monto, descripcion);
        deposito.ejecutar();
        transaccionRepository.guardar(deposito);
        puntosService.registrarPuntosPorTransaccion(deposito);
        deteccionAtipicasService.analizarYAlertar(deposito);
        return deposito;
    }

    public Retiro retirar(UUID monederoId, BigDecimal monto, String descripcion) {
        Monedero origen = monederoService.buscarPorId(monederoId);
        BigDecimal comision = calcularComisionRetiro(origen.getPropietario(), monto);
        Retiro retiro = new Retiro(origen, monto, comision, descripcion);
        retiro.ejecutar();
        transaccionRepository.guardar(retiro);
        puntosService.registrarPuntosPorTransaccion(retiro);
        deteccionAtipicasService.analizarYAlertar(retiro);
        verificarSaldoBajo(origen);
        return retiro;
    }

    public Transferencia transferir(UUID origenId, UUID destinoId, BigDecimal monto, String descripcion) {
        Monedero origen = monederoService.buscarPorId(origenId);
        Monedero destino = monederoService.buscarPorId(destinoId);
        boolean entrePropios = origen.getPropietario() != null
                && destino.getPropietario() != null
                && origen.getPropietario().getId().equals(destino.getPropietario().getId());
        BigDecimal comision = entrePropios
                ? BigDecimal.ZERO
                : calcularComisionTransferencia(origen.getPropietario(), monto);
        Transferencia transferencia = new Transferencia(origen, destino, monto, comision, descripcion);
        transferencia.ejecutar();
        transaccionRepository.guardar(transferencia);
        puntosService.registrarPuntosPorTransaccion(transferencia);
        deteccionAtipicasService.analizarYAlertar(transferencia);
        verificarSaldoBajo(origen);
        return transferencia;
    }

    public void revertirTransaccion(UUID transaccionId) {
        Transaccion transaccion = transaccionRepository.buscarPorId(transaccionId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Transaccion no encontrada."));
        transaccion.revertir();
        transaccionRepository.guardar(transaccion);
        puntosService.revertirPuntosDeTransaccion(transaccion);
    }

    public List<Transaccion> obtenerHistorialCliente(UUID clienteId) {
        return transaccionRepository.obtenerPorCliente(clienteId);
    }

    public List<Transaccion> filtrarHistorial(UUID clienteId, TipoTransaccion tipo) {
        return transaccionRepository.obtenerPorCliente(clienteId).stream()
                .filter(t -> tipo == null || t.obtenerTipo() == tipo)
                .collect(Collectors.toList());
    }

    public List<Transaccion> listarTodas() {
        return transaccionRepository.obtenerTodas();
    }

    public BigDecimal calcularComisionRetiro(Cliente cliente, BigDecimal monto) {
        if (monto == null) {
            return BigDecimal.ZERO;
        }
        if (cliente != null && tieneExencionRetiroVigente(cliente)) {
            return BigDecimal.ZERO;
        }
        BigDecimal base = monto.multiply(TASA_COMISION_RETIRO);
        if (cliente != null) {
            double descRango = cliente.getRango().getDescuentoComision();
            base = base.multiply(BigDecimal.valueOf(1.0 - descRango));
        }
        return base.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularComisionTransferencia(Cliente cliente, BigDecimal monto) {
        if (monto == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal base = monto.multiply(TASA_COMISION_TRANSFERENCIA);
        if (cliente != null) {
            double descRango = cliente.getRango().getDescuentoComision();
            base = base.multiply(BigDecimal.valueOf(1.0 - descRango));
            if (tieneDescuentoTransferencia(cliente)) {
                base = base.multiply(new BigDecimal("0.90"));
            }
        }
        return base.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean tieneExencionRetiroVigente(Cliente cliente) {
        LocalDateTime haceUnMes = LocalDateTime.now().minusDays(30);
        return cliente.getCanjes().stream()
                .anyMatch(c -> c.getEstado() == EstadoCanje.APLICADO
                        && c.getBeneficio().getTipo() == TipoBeneficio.EXENCION_CARGOS_RETIRO
                        && c.getFechaCanje().isAfter(haceUnMes));
    }

    private boolean tieneDescuentoTransferencia(Cliente cliente) {
        return cliente.getCanjes().stream()
                .anyMatch(c -> c.getEstado() == EstadoCanje.APLICADO
                        && c.getBeneficio().getTipo() == TipoBeneficio.DESCUENTO_COMISION_TRANSFERENCIA);
    }

    private void verificarSaldoBajo(Monedero monedero) {
        if (monedero.consultarSaldo().compareTo(UMBRAL_SALDO_BAJO) < 0) {
            notificacionService.notificarCliente(
                    monedero.getPropietario(),
                    "Saldo bajo en " + monedero.getNombre() + ": $" + monedero.consultarSaldo(),
                    TipoAlerta.SALDO_BAJO,
                    "App Local"
            );
        }
    }
}
