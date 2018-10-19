package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class AmountEntity extends Amount {

    public AmountEntity(@JsonProperty("Currency") String currency, @JsonProperty("Amount") double value) {
        super(currency, value);
    }
}
