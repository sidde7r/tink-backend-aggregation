package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AccountInfoEntity {
    @JsonProperty("iban")
    private String iban;

    @JsonCreator
    public AccountInfoEntity(@JsonProperty("iban") String iban) {
        this.iban = iban;
    }
}
