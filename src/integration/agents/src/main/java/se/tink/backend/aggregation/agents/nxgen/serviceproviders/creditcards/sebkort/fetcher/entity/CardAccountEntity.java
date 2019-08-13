package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardAccountEntity {
    private String id;
    private BigDecimal creditLimit;
    private BigDecimal purchaseLimit;
    private BigDecimal currentBalance;
    private BigDecimal nonBilledAmount;
    private BigDecimal disposableAmount;
    private String state;
    private String currencyCode;

    public String getId() {
        return id;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getPurchaseLimit() {
        return purchaseLimit;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getNonBilledAmount() {
        return nonBilledAmount;
    }

    public BigDecimal getDisposableAmount() {
        return disposableAmount;
    }

    public String getState() {
        return state;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
