package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {

    @JsonProperty("currency_code")
    private String currencyCode;

    private String value;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getValue() {
        return value;
    }

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(StringUtils.parseAmount(value), currencyCode);
    }
}
