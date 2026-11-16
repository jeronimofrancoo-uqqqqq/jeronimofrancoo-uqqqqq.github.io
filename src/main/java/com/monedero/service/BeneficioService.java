package com.monedero.service;

import com.monedero.domain.exception.EntidadNoEncontradaException;
import com.monedero.domain.exception.PuntosInsuficientesException;
import com.monedero.domain.model.Beneficio;
import com.monedero.domain.model.CanjeBeneficio;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.Monedero;
import com.monedero.domain.model.PuntosMovimiento;
import com.monedero.domain.model.TipoBeneficio;
import com.monedero.domain.model.TipoMovimientoPuntos;
import com.monedero.repository.BeneficioRepository;
import com.monedero.repository.ClienteRepository;

import java.util.List;
import java.util.UUID;

public class BeneficioService {
    private final BeneficioRepository beneficioRepository;
    private final ClienteRepository clienteRepository;
    private final MonederoService monederoService;

    public BeneficioService(BeneficioRepository beneficioRepository, ClienteRepository clienteRepository,
                            MonederoService monederoService) {
        this.beneficioRepository = beneficioRepository;
        this.clienteRepository = clienteRepository;
        this.monederoService = monederoService;
    }

    public List<Beneficio> listarDisponibles() {
        return beneficioRepository.obtenerActivos();
    }

    public CanjeBeneficio canjearBeneficio(UUID clienteId, UUID beneficioId) {
        Cliente cliente = clienteRepository.buscarPorId(clienteId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Cliente no encontrado."));
        Beneficio beneficio = beneficioRepository.buscarPorId(beneficioId)
                .orElseThrow(() -> new EntidadNoEncontradaException("Beneficio no encontrado."));

        if (!beneficio.estaDisponible()) {
            throw new IllegalStateException("El beneficio no esta disponible.");
        }
        if (cliente.getPuntosAcumulados() < beneficio.getCostoPuntos()) {
            throw new PuntosInsuficientesException(
                    "Puntos insuficientes: requiere " + beneficio.getCostoPuntos()
                            + " y tiene " + cliente.getPuntosAcumulados() + "."
            );
        }

        Monedero monederoBono = null;
        if (beneficio.getTipo() == TipoBeneficio.BONO_SALDO) {
            List<Monedero> activos = monederoService.obtenerActivosPorCliente(clienteId);
            if (activos.isEmpty()) {
                throw new EntidadNoEncontradaException("El cliente necesita un monedero activo para recibir el bono.");
            }
            monederoBono = activos.get(0);
        }

        CanjeBeneficio canje = new CanjeBeneficio(cliente, beneficio);
        aplicarBeneficio(canje, monederoBono);

        cliente.actualizarPuntos(-beneficio.getCostoPuntos());
        cliente.agregarMovimientoPuntos(new PuntosMovimiento(
                -beneficio.getCostoPuntos(),
                "Canje: " + beneficio.getNombre(),
                null,
                TipoMovimientoPuntos.CANJE
        ));
        cliente.agregarCanje(canje);
        clienteRepository.guardar(cliente);

        return canje;
    }

    public void aplicarBeneficio(CanjeBeneficio canje, Monedero monederoDestino) {
        canje.aplicar();
        if (canje.getBeneficio().getTipo() == TipoBeneficio.BONO_SALDO && monederoDestino != null) {
            monederoService.acreditar(monederoDestino.getId(), canje.getBeneficio().getValor());
        }
    }
}
