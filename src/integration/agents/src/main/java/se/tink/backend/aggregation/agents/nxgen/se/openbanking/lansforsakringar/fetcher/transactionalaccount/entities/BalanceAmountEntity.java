package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceAmountEntity {

    private Double amount;
    private String currency;

    public Amount toAmount() {
        return new Amount(currency, amount);
    }
}
