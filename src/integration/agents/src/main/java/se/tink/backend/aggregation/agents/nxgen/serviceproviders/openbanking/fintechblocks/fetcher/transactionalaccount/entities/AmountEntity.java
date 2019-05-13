package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {

    @JsonProperty("Amount")
    private Double amount;

    @JsonProperty("Currency")
    private String currency;

    public Amount toAmount() {
        return new Amount(currency, amount);
    }
}
