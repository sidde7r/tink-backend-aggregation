package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IbanEntity {

    @JsonProperty private final String iban;

    public IbanEntity(final String iban) {
        this.iban = iban;
    }
}
