package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoEntity {

    @JsonProperty private String iban;

    @JsonProperty private String currency;

    public AccountInfoEntity(String iban, String currency) {
        this.iban = iban;
        this.currency = currency;
    }
}
