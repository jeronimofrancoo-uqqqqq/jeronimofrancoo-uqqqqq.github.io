package com.monedero.service;

import com.monedero.domain.exception.EntidadNoEncontradaException;
import com.monedero.domain.model.Frecuencia;
import com.monedero.domain.model.Monedero;
import com.monedero.domain.model.TipoAlerta;
import com.monedero.domain.model.TipoTransaccion;
import com.monedero.domain.model.TransaccionProgramada;
import com.monedero.repository.ProgramadaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProgramacionService {
    private final ProgramadaRepository programadaRepository;
    private final TransaccionService transaccionService;
    private final NotificacionService notificacionService;

    public ProgramacionService(ProgramadaRepository programadaRepository,
                               TransaccionService transaccionService,
                               NotificacionService notificacionService) {
        this.programadaRepository = programadaRepository;
        this.transaccionService = transaccionService;
        this.notificacionService = notificacionService;
    }

    public TransaccionProgramada programar(TipoTransaccion tipo, BigDecimal monto, LocalDateTime proximaEjecucion,
                                           Frecuencia frecuencia, Monedero origen, Monedero destino,
                                           String descripcion) {
        TransaccionProgramada programada = new TransaccionProgramada(
                tipo, monto, proximaEjecucion, frecuencia, origen, destino, descripcion
        );
        programadaRepository.guardar(programada);
        return programada;
    }

    public void cancelar(UUID programadaId) {
        TransaccionProgramada programada = programadaRepository.buscarPorId(programadaId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Operacion programada no encontrada."));
        programada.cancelar();
        programadaRepository.guardar(programada);
    }

    // Ordenamiento por insercion por fecha de ejecucion (estable, O(n^2) peor caso)
    public List<TransaccionProgramada> obtenerOrdenadasPorFecha() {
        List<TransaccionProgramada> lista = new ArrayList<>(programadaRepository.obtenerActivas());
        for (int i = 1; i < lista.size(); i++) {
            TransaccionProgramada actual = lista.get(i);
            int j = i - 1;
            while (j >= 0 && lista.get(j).getProximaEjecucion().isAfter(actual.getProximaEjecucion())) {
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, actual);
        }
        return lista;
    }

    public List<TransaccionProgramada> obtenerOrdenadasPorCliente(UUID clienteId) {
        List<TransaccionProgramada> todas = obtenerOrdenadasPorFecha();
        List<TransaccionProgramada> resultado = new ArrayList<>();
        for (TransaccionProgramada prog : todas) {
            boolean esOrigen = prog.getMonederoOrigen() != null
                    && prog.getMonederoOrigen().getPropietario().getId().equals(clienteId);
            boolean esDestino = prog.getMonederoDestino() != null
                    && prog.getMonederoDestino().getPropietario().getId().equals(clienteId);
            if (esOrigen || esDestino) {
                resultado.add(prog);
            }
        }
        return resultado;
    }

    public List<TransaccionProgramada> listarTodas() {
        return programadaRepository.obtenerTodas();
    }

    public int ejecutarPendientes(LocalDateTime fechaReferencia) {
        List<TransaccionProgramada> ordenadas = obtenerOrdenadasPorFecha();
        int ejecutadas = 0;

        for (TransaccionProgramada prog : ordenadas) {
            if (!prog.getProximaEjecucion().isAfter(fechaReferencia)) {
                try {
                    ejecutarUna(prog);
                    prog.marcarEjecutada();
                    programadaRepository.guardar(prog);
                    ejecutadas++;
                } catch (Exception e) {
                    prog.setUltimoResultado("Error: " + e.getMessage());
                    programadaRepository.guardar(prog);
                    Monedero ref = prog.getMonederoOrigen() != null ? prog.getMonederoOrigen() : prog.getMonederoDestino();
                    if (ref != null && ref.getPropietario() != null) {
                        notificacionService.notificarCliente(
                                ref.getPropietario(),
                                "Fallo operacion programada (" + prog.getTipo() + "): " + e.getMessage(),
                                TipoAlerta.OPERACION_FALLIDA,
                                "App Local"
                        );
                    }
                }
            }
        }
        return ejecutadas;
    }

    private void ejecutarUna(TransaccionProgramada prog) {
        if (prog.getTipo() == TipoTransaccion.DEPOSITO) {
            transaccionService.depositar(prog.getMonederoDestino().getId(), prog.getMonto(), prog.getDescripcion());
        } else if (prog.getTipo() == TipoTransaccion.RETIRO) {
            transaccionService.retirar(prog.getMonederoOrigen().getId(), prog.getMonto(), prog.getDescripcion());
        } else if (prog.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            transaccionService.transferir(
                    prog.getMonederoOrigen().getId(),
                    prog.getMonederoDestino().getId(),
                    prog.getMonto(),
                    prog.getDescripcion()
            );
        }
        Monedero ref = prog.getMonederoOrigen() != null ? prog.getMonederoOrigen() : prog.getMonederoDestino();
        if (ref != null && ref.getPropietario() != null) {
            notificacionService.notificarCliente(
                    ref.getPropietario(),
                    "Recordatorio: se ejecuto " + prog.getTipo() + " programado por $" + prog.getMonto(),
                    TipoAlerta.TRANSACCION_PROGRAMADA,
                    "App Local"
            );
        }
    }
}
