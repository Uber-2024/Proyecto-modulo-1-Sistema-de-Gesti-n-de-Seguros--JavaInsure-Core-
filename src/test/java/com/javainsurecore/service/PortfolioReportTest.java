package com.javainsurecore.service;

import com.javainsurecore.model.CarInsurance;
import com.javainsurecore.model.Client;
import com.javainsurecore.model.HomeInsurance;
import com.javainsurecore.model.Insurance;
import com.javainsurecore.model.LifeInsurance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Comprueba los reportes de cartera y la búsqueda de pólizas por cliente. */
class PortfolioReportTest {
    private InsurancePortfolio portfolio;
    private PortfolioReport report;

    /** Prepara una cartera vacía y el servicio de reportes asociado. */
    @BeforeEach
    void setUp() {
        portfolio = new InsurancePortfolio();
        report = new PortfolioReport(portfolio, new InsuranceQuoter());
    }

    /** Verifica promedio, conteo por tipo y orden de las tres primas mayores. */
    @Test
    void reportsAverageCountsAndTopThreeByAnnualPremium() {
        portfolio.add(new LifeInsurance(new Client("L-1", "Ana", 61), new BigDecimal("10000")));
        portfolio.add(new CarInsurance(new Client("C-1", "Luis", 40), new BigDecimal("10000"), 2014));
        portfolio.add(new HomeInsurance(new Client("H-1", "Eva", 35), new BigDecimal("50000"), true));

        assertEquals(new BigDecimal("1216.67"), report.averagePremium());
        assertEquals(Map.of("Life", 1L, "Car", 1L, "Home", 1L), report.countByType());
        assertEquals(List.of("H-1", "C-1", "L-1"), report.topThreeMostExpensive().stream()
                .map(Insurance::client)
                .map(Client::id)
                .toList());
    }

    /** Verifica los resultados definidos para una cartera sin pólizas. */
    @Test
    void reportsZeroAverageAndNoTopPoliciesForEmptyPortfolio() {
        assertEquals(new BigDecimal("0.00"), report.averagePremium());
        assertEquals(Map.of(), report.countByType());
        assertEquals(List.of(), report.topThreeMostExpensive());
    }

    /** Verifica que la búsqueda por ID ignore diferencias entre mayúsculas. */
    @Test
    void searchesPoliciesByClientIdWithoutCaseSensitivity() {
        portfolio.add(new LifeInsurance(new Client("client-1", "Ana", 30), new BigDecimal("10000")));

        assertEquals(1, portfolio.findByClientId("CLIENT-1").size());
        assertEquals(0, portfolio.findByClientId("missing").size());
    }
}
