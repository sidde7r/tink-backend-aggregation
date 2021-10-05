package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AmountEntity {
    private BigDecimal amount;
    private String currency;

    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
