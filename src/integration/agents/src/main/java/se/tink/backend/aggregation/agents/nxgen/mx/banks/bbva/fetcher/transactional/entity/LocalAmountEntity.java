package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class LocalAmountEntity {
    private double amount;
    private String currency;

    public Amount toTinkAmount() {
        return new Amount(currency, amount);
    }
}
