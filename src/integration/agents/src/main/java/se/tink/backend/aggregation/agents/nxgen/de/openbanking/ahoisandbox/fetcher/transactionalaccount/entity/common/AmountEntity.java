package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.entity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {

    private Double value;
    private String currency;

    @JsonIgnore
    public Amount toTinkAmount() {
        return new Amount(currency, value);
    }
}
