package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoEntity {

    @JsonProperty private String iban;

    @JsonProperty private String currency;

    @JsonProperty private String bban;

    @JsonProperty private String msisdn;

    public AccountInfoEntity(String iban, String bban, String msisdn, String currency) {
        this.iban = iban;
        this.bban = bban;
        this.msisdn = msisdn;
        this.currency = currency;
    }
}
