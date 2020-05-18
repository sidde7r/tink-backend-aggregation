package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Account {

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("currency")
    private String currency;

    @Override
    public String toString() {
        return "Account{" + "iban = '" + iban + '\'' + ",currency = '" + currency + '\'' + "}";
    }
}
