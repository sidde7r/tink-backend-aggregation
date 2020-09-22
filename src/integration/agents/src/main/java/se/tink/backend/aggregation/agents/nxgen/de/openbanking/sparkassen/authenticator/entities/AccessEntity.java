package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    private String availableAccountsWithBalance;

    public AccessEntity() {
        this.availableAccountsWithBalance = "allAccounts";
    }
}
