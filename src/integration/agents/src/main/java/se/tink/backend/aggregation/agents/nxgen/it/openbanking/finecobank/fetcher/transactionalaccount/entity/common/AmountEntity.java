package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.common;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {

    private String currency;
    private double amount;

    public Amount toTinkAmount() {
        return new Amount(currency, amount);
    }
}
