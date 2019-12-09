package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountDetailsEntity {
    private String accountId;
    private String iban;

    public AccountDetailsEntity(String accountId, String iban) {
        this.accountId = accountId;
        this.iban = iban;
    }
}
