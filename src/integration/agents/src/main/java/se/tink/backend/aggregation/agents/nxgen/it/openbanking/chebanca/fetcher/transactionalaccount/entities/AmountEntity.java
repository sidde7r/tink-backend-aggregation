package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private Double amount;
    private String currency;

    @JsonCreator
    public AmountEntity(
            @JsonProperty("amount") Double amount, @JsonProperty("currency") String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    @JsonIgnore
    public Amount toAmount() {
        return new Amount(currency, amount);
    }
}
