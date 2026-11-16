package com.monedero.domain.exception;

public class EntidadNoEncontradaException extends RuntimeException {

    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
    }

    public EntidadNoEncontradaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
