package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentValuesEntity {
    private InvestmentAmountEntity investmentValueAmount;

    public InvestmentValuesEntity(String currency, BigDecimal amount) {
        this.investmentValueAmount = new InvestmentAmountEntity(currency, amount);
    }
}
