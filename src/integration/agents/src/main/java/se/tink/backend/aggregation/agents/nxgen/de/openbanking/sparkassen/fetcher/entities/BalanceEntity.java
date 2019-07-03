package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {
    private String balanceType;
    private BalanceAmountEntity balanceAmount;
    private String referenceDate;

    public BalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }
}
