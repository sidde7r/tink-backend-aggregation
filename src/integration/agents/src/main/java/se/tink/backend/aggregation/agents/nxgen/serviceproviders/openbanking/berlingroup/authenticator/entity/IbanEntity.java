package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class IbanEntity {

    @JsonProperty private String iban;

    public IbanEntity(String iban) {
        this.iban = iban;
    }

    public String toForm() {
        return Form.builder().put("iban", iban).build().serialize();
    }
}
