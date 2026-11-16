package com.monedero.service;

import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.Deposito;
import com.monedero.domain.model.Monedero;
import com.monedero.domain.model.RangoCliente;
import com.monedero.domain.model.Retiro;
import com.monedero.domain.model.Transferencia;
import com.monedero.repository.memory.InMemoryClienteRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuntosServiceTest {

    @Test
    void debeCalcularPuntosPorTipoYActualizarHistorialYRango() {
        InMemoryClienteRepository repo = new InMemoryClienteRepository();
        PuntosService service = new PuntosService(repo);
        Cliente cliente = new Cliente("Mario", "mario@correo.com", "300");
        repo.guardar(cliente);

        Monedero m1 = new Monedero("Cuenta 1", new BigDecimal("2000.00"), cliente);
        Monedero m2 = new Monedero("Cuenta 2", new BigDecimal("500.00"), cliente);

        Deposito dep = new Deposito(m1, new BigDecimal("500.00"), "Dep");
        Retiro ret = new Retiro(m1, new BigDecimal("500.00"), "Ret");
        Transferencia tr = new Transferencia(m1, m2, new BigDecimal("17000.00"), "Tr");

        assertEquals(5, service.registrarPuntosPorTransaccion(dep));
        assertEquals(10, service.registrarPuntosPorTransaccion(ret));
        assertEquals(510, service.registrarPuntosPorTransaccion(tr));

        assertEquals(525, cliente.getPuntosAcumulados());
        assertEquals(RangoCliente.PLATA, cliente.getRango());
        assertEquals(3, cliente.getHistorialPuntos().size());

        service.revertirPuntosDeTransaccion(ret);
        assertEquals(515, cliente.getPuntosAcumulados());
    }
}
