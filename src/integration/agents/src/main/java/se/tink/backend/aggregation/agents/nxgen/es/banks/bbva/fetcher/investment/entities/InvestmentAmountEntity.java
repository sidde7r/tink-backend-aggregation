package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentAmountEntity {
    private BigDecimal amount;
    private String currency;

    public InvestmentAmountEntity() {}

    public InvestmentAmountEntity(String currency, BigDecimal amount) {
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
