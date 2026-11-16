package com.monedero.service;

import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.Frecuencia;
import com.monedero.domain.model.Monedero;
import com.monedero.domain.model.TipoTransaccion;
import com.monedero.domain.model.TransaccionProgramada;
import com.monedero.repository.memory.InMemoryClienteRepository;
import com.monedero.repository.memory.InMemoryMonederoRepository;
import com.monedero.repository.memory.InMemoryProgramadaRepository;
import com.monedero.repository.memory.InMemoryTransaccionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgramacionServiceTest {

    @Test
    void debeOrdenarPorFechaYProcesarRecurrentesYFallos() {
        InMemoryClienteRepository clienteRepo = new InMemoryClienteRepository();
        InMemoryMonederoRepository monederoRepo = new InMemoryMonederoRepository();
        InMemoryTransaccionRepository txRepo = new InMemoryTransaccionRepository();
        InMemoryProgramadaRepository progRepo = new InMemoryProgramadaRepository();

        ClienteService clienteService = new ClienteService(clienteRepo);
        MonederoService monederoService = new MonederoService(monederoRepo, clienteService);
        NotificacionService notificacionService = new NotificacionService(clienteRepo);
        PuntosService puntosService = new PuntosService(clienteRepo);
        DeteccionAtipicasService atipicasService = new DeteccionAtipicasService(txRepo, notificacionService);
        TransaccionService txService = new TransaccionService(txRepo, monederoService, puntosService, atipicasService, notificacionService);
        ProgramacionService progService = new ProgramacionService(progRepo, txService, notificacionService);

        Cliente cliente = clienteService.registrarCliente("Luis", "luis@correo.com", "300");
        Monedero monedero = monederoService.crearMonedero(cliente.getId(), "Ahorro", new BigDecimal("100.00"));

        LocalDateTime ahora = LocalDateTime.now();
        progService.programar(TipoTransaccion.DEPOSITO, new BigDecimal("50.00"), ahora.plusDays(3), Frecuencia.DIARIA, null, monedero, "Futura");
        TransaccionProgramada pronta = progService.programar(TipoTransaccion.DEPOSITO, new BigDecimal("200.00"), ahora.minusHours(1), Frecuencia.MENSUAL, null, monedero, "Vencida");
        TransaccionProgramada fallida = progService.programar(TipoTransaccion.RETIRO, new BigDecimal("9000.00"), ahora.minusMinutes(10), Frecuencia.UNICA, monedero, null, "Sin fondos");

        List<TransaccionProgramada> ordenadas = progService.obtenerOrdenadasPorFecha();
        assertEquals(pronta.getId(), ordenadas.get(0).getId());
        assertEquals(fallida.getId(), ordenadas.get(1).getId());

        int ejecutadas = progService.ejecutarPendientes(ahora);
        assertEquals(1, ejecutadas);
        assertEquals(new BigDecimal("300.00"), monedero.consultarSaldo());
        assertTrue(fallida.getUltimoResultado().startsWith("Error:"));
    }
}
