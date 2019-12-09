package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IbanEntity {

    private String iban;

    @JsonCreator
    public IbanEntity(@JsonProperty("iban") String iban) {
        this.iban = iban;
    }
}
