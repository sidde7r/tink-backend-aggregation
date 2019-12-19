package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReturnsEntity {
    private InvestmentAmountEntity balanceAmount;
    private InvestmentAmountEntity profitAmount;
    private InvestmentAmountEntity dailyProfitAmount;
    private InvestmentAmountEntity netContributionAmount;
    private InvestmentAmountEntity dailyNetContributionAmount;
    private BigDecimal profitabilityInvest;
    private BigDecimal profitabilityProduct;

    public BigDecimal getProfitAmount() {
        return profitAmount.getAmount();
    }
}
