package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAmountEntity {

    private Double amount;
    private String currency;

    public Double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
