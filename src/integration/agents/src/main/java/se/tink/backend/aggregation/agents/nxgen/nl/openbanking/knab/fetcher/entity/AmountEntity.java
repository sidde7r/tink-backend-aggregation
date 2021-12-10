package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@SuppressWarnings("UnusedDeclaration")
public class AmountEntity {
    private String currency;
    private BigDecimal amount;

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
