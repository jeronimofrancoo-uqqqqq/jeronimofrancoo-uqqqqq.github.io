package com.monedero.domain.interfaces;

import com.monedero.domain.model.Alerta;

public interface Notificable {
    void enviar(Alerta alerta);
}
