package com.javainsurecore.service;

import com.javainsurecore.model.CarInsurance;
import com.javainsurecore.model.HomeInsurance;
import com.javainsurecore.model.Insurance;
import com.javainsurecore.model.LifeInsurance;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Calcula las primas anuales según el tipo de póliza y sus reglas de negocio. */
public class InsuranceQuoter {
    private static final BigDecimal LIFE_RATE = new BigDecimal("0.05");
    private static final BigDecimal SENIOR_SURCHARGE = new BigDecimal("1.20");
    private static final BigDecimal CAR_RATE = new BigDecimal("0.10");
    private static final BigDecimal OLD_CAR_SURCHARGE = new BigDecimal("50.00");
    private static final BigDecimal HOME_RATE = new BigDecimal("0.02");
    private static final int OLD_CAR_YEAR_LIMIT = 2015;

    /**
     * Calcula la prima anual y la redondea a dos decimales con la regla HALF_UP.
     *
     * @param insurance póliza que se desea cotizar
     * @return prima anual en la moneda del sistema
     */
    public BigDecimal calculateAnnualPremium(Insurance insurance) {
        return (switch (insurance) {
            case LifeInsurance life -> life.insuredAmount()
                    .multiply(LIFE_RATE)
                    .multiply(life.client().age() > 60 ? SENIOR_SURCHARGE : BigDecimal.ONE);
            case CarInsurance car -> car.insuredAmount()
                    .multiply(CAR_RATE)
                    .add(car.modelYear() < OLD_CAR_YEAR_LIMIT ? OLD_CAR_SURCHARGE : BigDecimal.ZERO);
            case HomeInsurance home -> home.insuredAmount()
                    .multiply(HOME_RATE)
                    .multiply(home.riskZone() ? BigDecimal.valueOf(2) : BigDecimal.ONE);
        }).setScale(2, RoundingMode.HALF_UP);
    }

}
