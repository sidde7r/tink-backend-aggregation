package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionAmountEntity {
    private String currency;
    private Number amount;

    @JsonIgnore
    public ExactCurrencyAmount toAmount() {
        if (Objects.isNull(amount)) {
            throw new IllegalStateException("Balance amount is not available");
        }
        return ExactCurrencyAmount.of((amount), currency);
    }
}
