package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceAmountEntity {
    @Getter private String currency;
    @Getter private String amount;

    @JsonIgnore
    public ExactCurrencyAmount toAmount() {
        if (amount == null) {
            throw new IllegalStateException("Balance amount is not available");
        }
        return ExactCurrencyAmount.of(Double.parseDouble(amount), currency);
    }
}
