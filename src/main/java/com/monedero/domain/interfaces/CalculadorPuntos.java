package com.monedero.domain.interfaces;

import com.monedero.domain.model.Transaccion;

public interface CalculadorPuntos {
    int calcularPuntos(Transaccion transaccion);
}
