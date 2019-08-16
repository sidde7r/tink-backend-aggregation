package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoEntity {
    @JsonProperty private String iban;

    @JsonProperty private String currency;

    @JsonProperty private String bban;

    @JsonProperty private String msisdn;

    @JsonCreator
    public AccountInfoEntity(
            @JsonProperty("iban") String iban,
            @JsonProperty("bban") String bban,
            @JsonProperty("msisdn") String msisdn,
            @JsonProperty("currency") String currency) {
        this.iban = iban;
        this.bban = bban;
        this.msisdn = msisdn;
        this.currency = currency;
    }
}
