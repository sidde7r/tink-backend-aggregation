package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {

    private String currency;
    private double amount;

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
