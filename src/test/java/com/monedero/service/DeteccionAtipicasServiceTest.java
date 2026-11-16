package com.monedero.service;

import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.Deposito;
import com.monedero.domain.model.Monedero;
import com.monedero.repository.memory.InMemoryClienteRepository;
import com.monedero.repository.memory.InMemoryMonederoRepository;
import com.monedero.repository.memory.InMemoryTransaccionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeteccionAtipicasServiceTest {

    @Test
    void debeCalcularUmbralYDetectarTransaccionAtipica() {
        InMemoryClienteRepository clienteRepo = new InMemoryClienteRepository();
        InMemoryMonederoRepository monederoRepo = new InMemoryMonederoRepository();
        InMemoryTransaccionRepository txRepo = new InMemoryTransaccionRepository();

        ClienteService clienteService = new ClienteService(clienteRepo);
        MonederoService monederoService = new MonederoService(monederoRepo, clienteService);
        NotificacionService notificacionService = new NotificacionService(clienteRepo);
        PuntosService puntosService = new PuntosService(clienteRepo);
        DeteccionAtipicasService atipicasService = new DeteccionAtipicasService(txRepo, notificacionService);
        TransaccionService txService = new TransaccionService(txRepo, monederoService, puntosService, atipicasService, notificacionService);

        Cliente cliente = clienteService.registrarCliente("Camila", "camila@correo.com", "300");
        Monedero monedero = monederoService.crearMonedero(cliente.getId(), "Principal", new BigDecimal("500.00"));

        // Con menos de 3 movimientos previos no se marca como atipica
        Deposito d1 = txService.depositar(monedero.getId(), new BigDecimal("100.00"), "1");
        assertFalse(d1.isSospechosa());
        txService.depositar(monedero.getId(), new BigDecimal("100.00"), "2");
        txService.depositar(monedero.getId(), new BigDecimal("100.00"), "3");

        List<Double> montos = List.of(100.0, 100.0, 100.0);
        assertEquals(100.0, atipicasService.calcularPromedio(montos), 0.01);
        assertEquals(0.0, atipicasService.calcularDesviacionEstandar(montos, 100.0), 0.01);
        assertEquals(100.0, atipicasService.calcularUmbral(montos), 0.01);

        Deposito atipico = txService.depositar(monedero.getId(), new BigDecimal("5000.00"), "Inusual");
        assertTrue(atipico.isSospechosa());
        assertFalse(cliente.getAlertas().isEmpty());
    }
}
