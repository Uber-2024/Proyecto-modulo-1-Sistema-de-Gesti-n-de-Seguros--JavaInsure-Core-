package com.javainsurecore.exception;

/** Indica que no fue posible leer o interpretar los datos de una póliza. */
public class InvalidPolicyDataException extends Exception {
    /**
     * Crea el error con una descripción.
     *
     * @param message explicación del problema de datos
     */
    public InvalidPolicyDataException(String message) {
        super(message);
    }

    /**
     * Crea el error con una descripción y la causa original.
     *
     * @param message explicación del problema de datos
     * @param cause excepción que originó el error
     */
    public InvalidPolicyDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
