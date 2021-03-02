package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceAmountEntity {

    private Double amount;
    private String currency;

    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
