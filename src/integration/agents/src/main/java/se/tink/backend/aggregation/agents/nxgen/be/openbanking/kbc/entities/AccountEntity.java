package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("currency")
    private String currency;

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }
}
