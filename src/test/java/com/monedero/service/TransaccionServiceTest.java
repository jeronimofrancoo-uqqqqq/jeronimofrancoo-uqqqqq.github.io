package com.monedero.service;

import com.monedero.domain.exception.SaldoInsuficienteException;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.Deposito;
import com.monedero.domain.model.EstadoTransaccion;
import com.monedero.domain.model.Monedero;
import com.monedero.domain.model.Transferencia;
import com.monedero.repository.memory.InMemoryClienteRepository;
import com.monedero.repository.memory.InMemoryMonederoRepository;
import com.monedero.repository.memory.InMemoryTransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransaccionServiceTest {
    private ClienteService clienteService;
    private MonederoService monederoService;
    private TransaccionService transaccionService;
    private Cliente clienteA;
    private Cliente clienteB;
    private Monedero monederoA;
    private Monedero monederoB;

    @BeforeEach
    void setUp() {
        InMemoryClienteRepository clienteRepo = new InMemoryClienteRepository();
        InMemoryMonederoRepository monederoRepo = new InMemoryMonederoRepository();
        InMemoryTransaccionRepository txRepo = new InMemoryTransaccionRepository();

        clienteService = new ClienteService(clienteRepo);
        monederoService = new MonederoService(monederoRepo, clienteService);
        NotificacionService notificacionService = new NotificacionService(clienteRepo);
        PuntosService puntosService = new PuntosService(clienteRepo);
        DeteccionAtipicasService atipicasService = new DeteccionAtipicasService(txRepo, notificacionService);

        transaccionService = new TransaccionService(txRepo, monederoService, puntosService, atipicasService, notificacionService);

        clienteA = clienteService.registrarCliente("Cliente A", "a@correo.com", "111");
        clienteB = clienteService.registrarCliente("Cliente B", "b@correo.com", "222");
        monederoA = monederoService.crearMonedero(clienteA.getId(), "Principal A", new BigDecimal("1000.00"));
        monederoB = monederoService.crearMonedero(clienteB.getId(), "Principal B", new BigDecimal("200.00"));
    }

    @Test
    void debeEjecutarDepositoRetiroYTransferencia() {
        Deposito dep = transaccionService.depositar(monederoA.getId(), new BigDecimal("500.00"), "Deposito");
        assertEquals(new BigDecimal("1500.00"), monederoA.consultarSaldo());
        assertEquals(5, dep.getPuntosGenerados());

        transaccionService.retirar(monederoA.getId(), new BigDecimal("100.00"), "Retiro");
        assertEquals(new BigDecimal("1399.00"), monederoA.consultarSaldo());

        Transferencia tr = transaccionService.transferir(monederoA.getId(), monederoB.getId(), new BigDecimal("200.00"), "Pago");
        assertEquals(new BigDecimal("1195.00"), monederoA.consultarSaldo());
        assertEquals(new BigDecimal("400.00"), monederoB.consultarSaldo());
        assertEquals(EstadoTransaccion.COMPLETADA, tr.getEstado());
    }

    @Test
    void noDebeDejarCambiosParcialesSiSaldoEsInsuficiente() {
        assertThrows(SaldoInsuficienteException.class, () ->
                transaccionService.transferir(monederoB.getId(), monederoA.getId(), new BigDecimal("500.00"), "Excedido"));
        assertEquals(new BigDecimal("200.00"), monederoB.consultarSaldo());
        assertEquals(new BigDecimal("1000.00"), monederoA.consultarSaldo());
    }

    @Test
    void debeRevertirTransaccionYPuntos() {
        Deposito dep = transaccionService.depositar(monederoA.getId(), new BigDecimal("400.00"), "Recarga");
        assertEquals(4, clienteA.getPuntosAcumulados());

        transaccionService.revertirTransaccion(dep.getId());
        assertEquals(EstadoTransaccion.REVERTIDA, dep.getEstado());
        assertEquals(new BigDecimal("1000.00"), monederoA.consultarSaldo());
        assertEquals(0, clienteA.getPuntosAcumulados());
    }

    @Test
    void debePermitirTransferirEntreMonederosDelMismoClienteSinComision() {
        Monedero ahorrosA = monederoService.crearMonedero(clienteA.getId(), "Ahorros A", new BigDecimal("250.00"));
        Transferencia interna = transaccionService.transferir(
                monederoA.getId(), ahorrosA.getId(), new BigDecimal("300.00"), "Mover a ahorros"
        );
        assertEquals(new BigDecimal("0.00"), interna.getComision());
        assertEquals(new BigDecimal("700.00"), monederoA.consultarSaldo());
        assertEquals(new BigDecimal("550.00"), ahorrosA.consultarSaldo());
    }
}
