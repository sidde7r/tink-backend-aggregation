package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    @JsonProperty private String currency;
    @JsonProperty private String amount;

    @JsonIgnore
    public Amount toTinkAmount() {
        return new Amount(currency, Double.parseDouble(amount));
    }
}
