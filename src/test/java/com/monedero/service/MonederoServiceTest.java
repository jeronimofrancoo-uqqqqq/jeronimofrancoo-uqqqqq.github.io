package com.monedero.service;

import com.monedero.domain.exception.MontoInvalidoException;
import com.monedero.domain.exception.SaldoInsuficienteException;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.Monedero;
import com.monedero.repository.memory.InMemoryClienteRepository;
import com.monedero.repository.memory.InMemoryMonederoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonederoServiceTest {
    private ClienteService clienteService;
    private MonederoService monederoService;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(new InMemoryClienteRepository());
        monederoService = new MonederoService(new InMemoryMonederoRepository(), clienteService);
        cliente = clienteService.registrarCliente("Sofia Perez", "sofia@correo.com", "3000000000");
    }

    @Test
    void debeCrearMonederoYConsultarSaldo() {
        Monedero m = monederoService.crearMonedero(cliente.getId(), "Ahorros", new BigDecimal("250.00"));
        assertEquals(new BigDecimal("250.00"), monederoService.consultarSaldo(m.getId()));
    }

    @Test
    void debeAcreditarYRechazarMontosInvalidosOSaldoInsuficiente() {
        Monedero m = monederoService.crearMonedero(cliente.getId(), "Diario", new BigDecimal("100.00"));
        monederoService.acreditar(m.getId(), new BigDecimal("50.00"));
        assertEquals(new BigDecimal("150.00"), monederoService.consultarSaldo(m.getId()));

        assertThrows(MontoInvalidoException.class, () -> monederoService.acreditar(m.getId(), new BigDecimal("-10.00")));
        assertThrows(SaldoInsuficienteException.class, () -> monederoService.debitar(m.getId(), new BigDecimal("200.00")));
    }
}
