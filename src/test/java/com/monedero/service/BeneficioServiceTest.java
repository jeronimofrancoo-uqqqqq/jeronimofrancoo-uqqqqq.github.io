package com.monedero.service;

import com.monedero.domain.exception.PuntosInsuficientesException;
import com.monedero.domain.model.Beneficio;
import com.monedero.domain.model.CanjeBeneficio;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.EstadoCanje;
import com.monedero.domain.model.Monedero;
import com.monedero.domain.model.TipoBeneficio;
import com.monedero.repository.memory.InMemoryBeneficioRepository;
import com.monedero.repository.memory.InMemoryClienteRepository;
import com.monedero.repository.memory.InMemoryMonederoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeneficioServiceTest {

    @Test
    void debeCanjearBonoYEvitarDobleAplicacion() {
        InMemoryClienteRepository clienteRepo = new InMemoryClienteRepository();
        InMemoryMonederoRepository monederoRepo = new InMemoryMonederoRepository();
        InMemoryBeneficioRepository beneficioRepo = new InMemoryBeneficioRepository();

        ClienteService clienteService = new ClienteService(clienteRepo);
        MonederoService monederoService = new MonederoService(monederoRepo, clienteService);
        BeneficioService beneficioService = new BeneficioService(beneficioRepo, clienteRepo, monederoService);

        Cliente cliente = clienteService.registrarCliente("Elena", "elena@correo.com", "300");
        cliente.actualizarPuntos(1200);
        clienteRepo.guardar(cliente);
        Monedero monedero = monederoService.crearMonedero(cliente.getId(), "Principal", new BigDecimal("100.00"));

        Beneficio bono = beneficioService.listarDisponibles().stream()
                .filter(b -> b.getTipo() == TipoBeneficio.BONO_SALDO)
                .findFirst()
                .orElseThrow();

        CanjeBeneficio canje = beneficioService.canjearBeneficio(cliente.getId(), bono.getId());
        assertEquals(EstadoCanje.APLICADO, canje.getEstado());
        assertEquals(200, cliente.getPuntosAcumulados());
        assertEquals(new BigDecimal("150.00"), monedero.consultarSaldo());

        assertThrows(IllegalStateException.class, canje::aplicar);
        assertThrows(PuntosInsuficientesException.class, () ->
                beneficioService.canjearBeneficio(cliente.getId(), bono.getId()));
    }
}
