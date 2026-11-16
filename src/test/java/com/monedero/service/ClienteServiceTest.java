package com.monedero.service;

import com.monedero.domain.exception.EntidadNoEncontradaException;
import com.monedero.domain.model.Cliente;
import com.monedero.repository.memory.InMemoryClienteRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClienteServiceTest {

    @Test
    void debeRegistrarYAutenticarCliente() {
        ClienteService service = new ClienteService(new InMemoryClienteRepository());
        Cliente c1 = service.registrarCliente("Laura Diaz", "laura@correo.com", "3001112233", "2468");

        Cliente autenticado = service.autenticar("laura@correo.com", "2468");
        assertEquals(c1.getId(), autenticado.getId());

        assertThrows(IllegalArgumentException.class, () -> service.autenticar("laura@correo.com", "0000"));
        assertThrows(IllegalArgumentException.class, () ->
                service.registrarCliente("Otra Laura", "laura@correo.com", "3009998888", "1111"));

        List<Cliente> filtrados = service.buscarPorFiltro("laura");
        assertEquals(1, filtrados.size());
    }

    @Test
    void debeLanzarExcepcionSiNoExisteCliente() {
        ClienteService service = new ClienteService(new InMemoryClienteRepository());
        assertThrows(EntidadNoEncontradaException.class, () -> service.buscarPorId(UUID.randomUUID()));
        assertThrows(EntidadNoEncontradaException.class, () -> service.autenticar("nadie@correo.com", "1234"));
    }
}
