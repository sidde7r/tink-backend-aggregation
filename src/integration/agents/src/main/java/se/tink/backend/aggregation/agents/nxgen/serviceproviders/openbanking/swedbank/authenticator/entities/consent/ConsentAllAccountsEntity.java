package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.consent;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAllAccountsEntity {
    private String availableAccounts;

    public ConsentAllAccountsEntity(String availableAccounts) {
        this.availableAccounts = availableAccounts;
    }
}
