package com.javainsurecore.model;

import com.javainsurecore.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Póliza de hogar que registra si el inmueble está en una zona de riesgo.
 *
 * @param client cliente titular
 * @param insuredAmount valor asegurado, que debe ser mayor que cero
 * @param riskZone indica si corresponde aplicar la tarifa duplicada por riesgo
 */
public record HomeInsurance(Client client, BigDecimal insuredAmount, boolean riskZone) implements Insurance {
    /** Valida que la póliza tenga un cliente y un monto asegurado positivo. */
    public HomeInsurance {
        Objects.requireNonNull(client, "El cliente es obligatorio.");
        if (insuredAmount == null || insuredAmount.signum() <= 0) {
            throw new BusinessRuleException("El monto asegurado debe ser mayor que cero.");
        }
    }
}
