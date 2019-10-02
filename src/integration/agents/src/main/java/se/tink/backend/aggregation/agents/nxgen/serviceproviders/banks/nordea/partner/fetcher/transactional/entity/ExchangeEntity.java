package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExchangeEntity {
    @JsonProperty("original_amount")
    private String originalAmount;

    @JsonProperty("original_currency")
    private String originalCurrency;

    @JsonProperty("original_rate")
    private String originalRate;

    @JsonProperty("payment_code")
    private String paymentCode;

    public String getOriginalAmount() {
        return originalAmount;
    }

    public String getOriginalCurrency() {
        return originalCurrency;
    }

    public String getOriginalRate() {
        return originalRate;
    }
}
