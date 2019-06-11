package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PsuAccountIdentificationEntity {
    @JsonProperty("iban")
    private String iban = null;

    @JsonProperty("currency")
    private String currency = null;

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }
}
