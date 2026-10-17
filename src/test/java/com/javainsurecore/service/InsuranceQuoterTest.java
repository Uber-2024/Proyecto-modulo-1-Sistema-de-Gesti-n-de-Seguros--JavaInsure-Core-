package com.javainsurecore.service;

import com.javainsurecore.exception.BusinessRuleException;
import com.javainsurecore.model.CarInsurance;
import com.javainsurecore.model.Client;
import com.javainsurecore.model.HomeInsurance;
import com.javainsurecore.model.LifeInsurance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Comprueba los cálculos de primas y validaciones básicas de las pólizas. */
class InsuranceQuoterTest {
    private InsuranceQuoter quoter;

    /** Crea una instancia nueva del cotizador para cada prueba. */
    @BeforeEach
    void setUp() {
        quoter = new InsuranceQuoter();
    }

    /** Verifica la tarifa estándar de vida hasta los 60 años inclusive. */
    @Test
    void calculatesLifePremiumForClientYoungerThanSixtyOne() {
        LifeInsurance insurance = new LifeInsurance(client(60), new BigDecimal("100000.00"));
        assertEquals(new BigDecimal("5000.00"), quoter.calculateAnnualPremium(insurance));
    }

    /** Verifica el recargo del 20 % para clientes mayores de 60 años. */
    @Test
    void addsTwentyPercentToLifePremiumWhenClientIsOlderThanSixty() {
        LifeInsurance insurance = new LifeInsurance(client(61), new BigDecimal("100000"));
        assertEquals(new BigDecimal("6000.00"), quoter.calculateAnnualPremium(insurance));
    }

    /** Verifica el recargo fijo aplicado a vehículos anteriores a 2015. */
    @Test
    void addsFixedSurchargeToCarOlderThan2015() {
        CarInsurance insurance = new CarInsurance(client(40), new BigDecimal("10000"), 2014);
        assertEquals(new BigDecimal("1050.00"), quoter.calculateAnnualPremium(insurance));
    }

    /** Verifica que el año 2015 no active el recargo por antigüedad. */
    @Test
    void doesNotAddSurchargeToCarFrom2015() {
        CarInsurance insurance = new CarInsurance(client(40), new BigDecimal("10000"), 2015);
        assertEquals(new BigDecimal("1000.00"), quoter.calculateAnnualPremium(insurance));
    }

    /** Verifica que la prima de hogar se duplique en zona de riesgo. */
    @Test
    void doublesHomePremiumInRiskZone() {
        HomeInsurance insurance = new HomeInsurance(client(35), new BigDecimal("250000"), true);
        assertEquals(new BigDecimal("10000.00"), quoter.calculateAnnualPremium(insurance));
    }

    /** Verifica la tarifa normal de hogar fuera de una zona de riesgo. */
    @Test
    void calculatesHomePremiumNormallyOutsideRiskZone() {
        HomeInsurance insurance = new HomeInsurance(client(35), new BigDecimal("250000"), false);
        assertEquals(new BigDecimal("5000.00"), quoter.calculateAnnualPremium(insurance));
    }

    /** Verifica que no se permita registrar una edad negativa. */
    @Test
    void rejectsNegativeClientAge() {
        assertThrows(BusinessRuleException.class, () -> client(-1));
    }

    /** Verifica que no se permita registrar una suma asegurada igual a cero. */
    @Test
    void rejectsZeroInsuredAmount() {
        assertThrows(BusinessRuleException.class,
                () -> new LifeInsurance(client(30), BigDecimal.ZERO));
    }

    /** Construye un cliente de prueba con los datos comunes del conjunto. */
    private Client client(int age) {
        return new Client("TEST-1", "Cliente de prueba", age);
    }
}
