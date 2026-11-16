package com.monedero.service;

import com.monedero.domain.interfaces.Notificable;
import com.monedero.domain.model.Alerta;
import com.monedero.domain.model.Cliente;
import com.monedero.domain.model.TipoAlerta;
import com.monedero.repository.ClienteRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificacionService implements Notificable {
    private final ClienteRepository clienteRepository;
    private final List<Alerta> registroGlobal = new ArrayList<>();

    public NotificacionService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void enviar(Alerta alerta) {
        if (alerta != null) {
            registroGlobal.add(alerta);
        }
    }

    // Notificacion simulada dentro de la aplicacion (no envia correos ni SMS reales)
    public Alerta notificarCliente(Cliente cliente, String mensaje, TipoAlerta tipo, String canalSimulado) {
        Alerta alerta = new Alerta(mensaje, tipo, canalSimulado);
        if (cliente != null) {
            cliente.agregarAlerta(alerta);
            clienteRepository.guardar(cliente);
        }
        enviar(alerta);
        return alerta;
    }

    public List<Alerta> obtenerAlertasGlobales() {
        return Collections.unmodifiableList(registroGlobal);
    }
}
