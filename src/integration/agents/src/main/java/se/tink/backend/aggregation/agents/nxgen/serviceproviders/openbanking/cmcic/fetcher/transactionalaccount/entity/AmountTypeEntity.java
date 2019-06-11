package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountTypeEntity {
    @JsonProperty("currency")
    private String currency = null;

    @JsonProperty("amount")
    private String amount = null;

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }
}
