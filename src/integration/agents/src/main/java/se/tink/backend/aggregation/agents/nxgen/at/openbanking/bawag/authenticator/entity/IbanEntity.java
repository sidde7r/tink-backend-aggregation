package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class IbanEntity {

    @JsonProperty private final String iban;

    public IbanEntity(final String iban) {
        this.iban = iban;
    }

    public String toForm() {
        return Form.builder().put("iban", iban).build().serialize();
    }
}
