package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IbanEntity {
    @JsonProperty private final String iban;
    @JsonProperty private final String currency;

    public IbanEntity(final String iban, final String currency) {
        this.currency = currency;
        this.iban = iban;
    }
}
