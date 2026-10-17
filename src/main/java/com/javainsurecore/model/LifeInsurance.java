package com.javainsurecore.model;

import com.javainsurecore.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Póliza de vida; el recargo por edad se calcula en el servicio de cotización.
 *
 * @param client cliente titular
 * @param insuredAmount monto asegurado, que debe ser mayor que cero
 */
public record LifeInsurance(Client client, BigDecimal insuredAmount) implements Insurance {
    /** Valida que la póliza tenga un cliente y un monto asegurado positivo. */
    public LifeInsurance {
        Objects.requireNonNull(client, "El cliente es obligatorio.");
        validateAmount(insuredAmount);
    }

    /** Rechaza montos nulos, iguales a cero o negativos. */
    private static void validateAmount(BigDecimal insuredAmount) {
        if (insuredAmount == null || insuredAmount.signum() <= 0) {
            throw new BusinessRuleException("El monto asegurado debe ser mayor que cero.");
        }
    }
}
