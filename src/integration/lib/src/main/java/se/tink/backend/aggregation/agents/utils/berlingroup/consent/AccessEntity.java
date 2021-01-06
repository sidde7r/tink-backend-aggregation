package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    private String availableAccountsWithBalance;

    public AccessEntity() {
        this.availableAccountsWithBalance = "allAccounts";
    }
}
