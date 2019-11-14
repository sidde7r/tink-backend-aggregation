package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity extends ExactCurrencyAmount {

    public AmountEntity(
            @JsonProperty("Currency") String currency, @JsonProperty("Amount") String value) {
        super(new BigDecimal(value), currency);
    }
}
