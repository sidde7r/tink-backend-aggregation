package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class BalanceEntity {

    public static final int SCALE = 2;

    private String currency;

    private long availableBalance;

    public String getCurrency() {
        return currency;
    }

    public long getAvailableBalance() {
        return availableBalance;
    }
}
