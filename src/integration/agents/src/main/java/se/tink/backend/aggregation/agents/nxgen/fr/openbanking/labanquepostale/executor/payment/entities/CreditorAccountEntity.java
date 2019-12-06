package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorAccountEntity {

    private String iban;

    public CreditorAccountEntity(String iban) {
        this.iban = iban;
    }

    public CreditorAccountEntity() {}

    public String getIban() {
        return iban;
    }
}
