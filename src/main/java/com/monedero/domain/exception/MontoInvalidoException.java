package com.monedero.domain.exception;

public class MontoInvalidoException extends RuntimeException {

    public MontoInvalidoException(String mensaje) {
        super(mensaje);
    }

    public MontoInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
