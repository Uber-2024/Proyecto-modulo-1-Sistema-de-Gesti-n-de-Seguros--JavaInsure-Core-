package com.javainsurecore.model;

import com.javainsurecore.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Póliza de automóvil con validación de su suma asegurada y año del vehículo.
 *
 * @param client cliente titular
 * @param insuredAmount valor asegurado, que debe ser mayor que cero
 * @param modelYear año de fabricación del vehículo
 */
public record CarInsurance(Client client, BigDecimal insuredAmount, int modelYear) implements Insurance {
    /** Verifica los datos requeridos antes de crear la póliza. */
    public CarInsurance {
        Objects.requireNonNull(client, "El cliente es obligatorio.");
        if (insuredAmount == null || insuredAmount.signum() <= 0) {
            throw new BusinessRuleException("El monto asegurado debe ser mayor que cero.");
        }
        if (modelYear < 1886) {
            throw new BusinessRuleException("El año del vehículo no es válido.");
        }
    }
}
