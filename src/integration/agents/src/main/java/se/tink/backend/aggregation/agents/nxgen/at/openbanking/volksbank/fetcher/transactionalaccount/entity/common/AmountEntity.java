package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.common;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {

    private String currency;
    private String amount;

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
