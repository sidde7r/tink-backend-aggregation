package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountHoldersEntity {
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public AccountHoldersEntity(String displayName) {
        this.displayName = displayName;
    }
}
