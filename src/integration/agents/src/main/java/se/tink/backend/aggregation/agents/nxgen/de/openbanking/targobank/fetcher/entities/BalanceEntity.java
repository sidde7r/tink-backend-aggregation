package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {
    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    public BalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }
}
