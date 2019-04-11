package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {

    private Double amount;

    private String currencyCode;

    public Amount toAmount() {
        return new Amount(currencyCode, amount);
    }
}
