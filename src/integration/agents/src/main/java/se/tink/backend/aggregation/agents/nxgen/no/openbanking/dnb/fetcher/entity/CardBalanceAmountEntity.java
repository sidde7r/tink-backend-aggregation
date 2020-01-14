package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardBalanceAmountEntity {

    private String amount;
    private String currency;

    public String getAmount() {
        return amount;
    }
}
