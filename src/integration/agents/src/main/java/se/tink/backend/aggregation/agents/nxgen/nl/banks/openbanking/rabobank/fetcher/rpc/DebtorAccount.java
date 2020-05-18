package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DebtorAccount {

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
