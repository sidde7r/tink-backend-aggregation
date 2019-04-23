package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity extends Amount {

    public AmountEntity(
            @JsonProperty("Currency") String currency, @JsonProperty("Amount") String value) {
        super(currency, StringUtils.parseAmount(value));
    }
}
