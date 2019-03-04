package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {
    private String balanceType;
    private BalanceAmountEntity balanceAmount;
    private String referenceDate;

    public String getBalanceType() {
        return balanceType;
    }

    public BalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public String getReferenceDate() {
        return referenceDate;
    }
}
