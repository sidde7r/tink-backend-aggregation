package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entity.consent;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAllAccountsEntity {
    private String availableAccounts;

    public ConsentAllAccountsEntity(String availableAccounts) {
        this.availableAccounts = availableAccounts;
    }
}
