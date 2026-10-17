package com.javainsurecore.model;

import com.javainsurecore.exception.BusinessRuleException;

/**
 * Representa a un cliente y valida los datos básicos requeridos para registrarlo.
 *
 * @param id identificador no vacío del cliente
 * @param name nombre no vacío del cliente
 * @param age edad del cliente, expresada en años y no negativa
 */
public record Client(String id, String name, int age) {
    /** Aplica las reglas de validez al crear el registro del cliente. */
    public Client {
        if (id == null || id.isBlank()) {
            throw new BusinessRuleException("El ID del cliente es obligatorio.");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("El nombre del cliente es obligatorio.");
        }
        if (age < 0) {
            throw new BusinessRuleException("La edad del cliente no puede ser negativa.");
        }
    }
}
