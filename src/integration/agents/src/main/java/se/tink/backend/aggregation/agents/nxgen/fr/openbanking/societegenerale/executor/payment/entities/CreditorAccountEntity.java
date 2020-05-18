package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorAccountEntity {

    private String iban;

    @JsonIgnore
    public CreditorAccountEntity(String iban) {
        this.iban = iban;
    }

    public CreditorAccountEntity() {}

    public String getIban() {
        return iban;
    }
}
