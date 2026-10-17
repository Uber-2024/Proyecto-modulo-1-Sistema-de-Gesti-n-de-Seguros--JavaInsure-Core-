package com.javainsurecore.service;

import com.javainsurecore.model.CarInsurance;
import com.javainsurecore.model.HomeInsurance;
import com.javainsurecore.model.Insurance;
import com.javainsurecore.model.LifeInsurance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Genera indicadores y listados gerenciales a partir de la cartera de pólizas. */
public class PortfolioReport {
    private final InsurancePortfolio portfolio;
    private final InsuranceQuoter quoter;

    /**
     * Prepara los reportes usando la cartera y el cotizador proporcionados.
     *
     * @param portfolio fuente de pólizas
     * @param quoter servicio utilizado para calcular primas
     */
    public PortfolioReport(InsurancePortfolio portfolio, InsuranceQuoter quoter) {
        this.portfolio = portfolio;
        this.quoter = quoter;
    }

    /**
     * Calcula la prima promedio; devuelve cero con dos decimales si no hay pólizas.
     *
     * @return promedio de las primas anuales
     */
    public BigDecimal averagePremium() {
        return portfolio.listAll().stream()
                .map(quoter::calculateAnnualPremium)
                .collect(Collectors.teeing(
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add),
                        Collectors.counting(),
                        (sum, count) -> count == 0
                                ? BigDecimal.ZERO.setScale(2)
                                : sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)));
    }

    /** @return cantidad de pólizas agrupada por tipo */
    public Map<String, Long> countByType() {
        return portfolio.listAll().stream()
                .collect(Collectors.groupingBy(
                        policy -> switch (policy) {
                            case LifeInsurance ignored -> "Life";
                            case CarInsurance ignored -> "Car";
                            case HomeInsurance ignored -> "Home";
                        },
                        Collectors.counting()));
    }

    /** @return hasta tres pólizas ordenadas desde la prima anual más alta */
    public List<Insurance> topThreeMostExpensive() {
        return portfolio.listAll().stream()
                .sorted((first, second) -> quoter.calculateAnnualPremium(second)
                        .compareTo(quoter.calculateAnnualPremium(first)))
                .limit(3)
                .toList();
    }
}
