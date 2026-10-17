package com.javainsurecore.exception;

/** Indica que una operación o dato incumple una regla del negocio. */
public class BusinessRuleException extends RuntimeException {
    /**
     * Crea la excepción con una descripción del incumplimiento.
     *
     * @param message explicación del error de negocio
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
