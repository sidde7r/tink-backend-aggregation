package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {

    private String amount;
    private String currency;

    @JsonCreator
    public AmountEntity(
            @JsonProperty("amount") String amount, @JsonProperty("currency") String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
