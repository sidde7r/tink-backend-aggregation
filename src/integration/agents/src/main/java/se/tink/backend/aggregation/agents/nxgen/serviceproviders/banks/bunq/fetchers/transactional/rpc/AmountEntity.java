package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {
    private String currency;
    private String value;

    public String getCurrency() {
        return currency;
    }

    public String getValue() {
        return value;
    }

    @JsonIgnore
    public ExactCurrencyAmount getAsTinkAmount() {
        return ExactCurrencyAmount.of(value, currency);
    }
}
