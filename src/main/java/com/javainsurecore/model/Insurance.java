package com.javainsurecore.model;

import java.math.BigDecimal;

/**
 * Contrato común para las pólizas admitidas por el sistema.
 * Solo permite las implementaciones de vida, automóvil y hogar.
 */
public sealed interface Insurance permits LifeInsurance, CarInsurance, HomeInsurance {
    /** @return cliente titular de la póliza */
    Client client();

    /** @return monto asegurado expresado como valor decimal */
    BigDecimal insuredAmount();
}
