package com.monedero.domain.exception;

public class PuntosInsuficientesException extends RuntimeException {

    public PuntosInsuficientesException(String mensaje) {
        super(mensaje);
    }

    public PuntosInsuficientesException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
