package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AllAccountsAccessEntity implements AccessEntity {

    private String availableAccounts;

    public AllAccountsAccessEntity(String availableAccounts) {
        this.availableAccounts = availableAccounts;
    }
}
