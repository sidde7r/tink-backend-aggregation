package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsEntity {
    private String iban;

    public AccountsEntity(String iban) {
        this.iban = iban;
    }
}
