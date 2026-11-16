package com.monedero.service;

import com.monedero.domain.exception.EntidadNoEncontradaException;
import com.monedero.domain.model.Cliente;
import com.monedero.repository.ClienteRepository;
import com.monedero.util.Validaciones;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente registrarCliente(String nombre, String correo, String telefono) {
        return registrarCliente(nombre, correo, telefono, "1234");
    }

    public Cliente registrarCliente(String nombre, String correo, String telefono, String pin) {
        Validaciones.validarTextoNoVacio(nombre, "nombre");
        Validaciones.validarTextoNoVacio(correo, "correo");
        Validaciones.validarTextoNoVacio(pin, "PIN");

        String correoNormalizado = correo.trim().toLowerCase();
        boolean existe = clienteRepository.obtenerTodos().stream()
                .anyMatch(c -> c.getCorreo().trim().toLowerCase().equals(correoNormalizado));
        if (existe) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con ese correo.");
        }

        String tel = telefono == null ? "" : telefono.trim();
        Cliente cliente = new Cliente(nombre.trim(), correo.trim(), tel, pin.trim());
        clienteRepository.guardar(cliente);
        return cliente;
    }

    public Cliente autenticar(String correo, String pin) {
        Validaciones.validarTextoNoVacio(correo, "correo");
        Validaciones.validarTextoNoVacio(pin, "PIN");
        String correoBuscado = correo.trim().toLowerCase();

        Cliente cliente = clienteRepository.obtenerTodos().stream()
                .filter(c -> c.getCorreo().trim().toLowerCase().equals(correoBuscado))
                .findFirst()
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe una cuenta con ese correo."));

        if (!cliente.validarPin(pin)) {
            throw new IllegalArgumentException("El PIN ingresado es incorrecto.");
        }
        return cliente;
    }

    public Cliente actualizarPerfil(UUID clienteId, String nombre, String telefono) {
        Validaciones.validarTextoNoVacio(nombre, "nombre");
        Cliente cliente = buscarPorId(clienteId);
        cliente.setNombre(nombre.trim());
        cliente.setTelefono(telefono == null ? "" : telefono.trim());
        clienteRepository.guardar(cliente);
        return cliente;
    }

    public Cliente buscarPorId(UUID id) {
        return clienteRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Cliente no encontrado."));
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.obtenerTodos();
    }

    public List<Cliente> buscarPorFiltro(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return listarTodos();
        }
        String criterio = texto.trim().toLowerCase();
        return clienteRepository.obtenerTodos().stream()
                .filter(c -> c.getNombre().toLowerCase().contains(criterio)
                        || c.getCorreo().toLowerCase().contains(criterio))
                .collect(Collectors.toList());
    }
}
