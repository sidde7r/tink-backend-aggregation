package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsEntity {

    @JsonProperty private final String iban;
    @JsonProperty private final String currency;

    public AccountsEntity(String iban, String currency) {
        this.iban = iban;
        this.currency = currency;
    }
}
