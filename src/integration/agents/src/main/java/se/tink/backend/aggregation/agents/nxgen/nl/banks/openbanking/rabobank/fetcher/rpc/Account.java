package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Account {

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("currency")
    private String currency;

    public void setIban(final String iban) {
        this.iban = iban;
    }

    public String getIban() {
        return iban;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return "AccountEntity{" + "iban = '" + iban + '\'' + ",currency = '" + currency + '\'' + "}";
    }
}
