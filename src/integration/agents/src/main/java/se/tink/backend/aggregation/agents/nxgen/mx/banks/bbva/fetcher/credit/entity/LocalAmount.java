package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class LocalAmount {
    private double amount;
    private String currency;

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
