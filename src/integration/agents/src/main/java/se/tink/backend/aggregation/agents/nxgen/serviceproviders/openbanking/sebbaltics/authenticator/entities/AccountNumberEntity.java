package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountNumberEntity {
    private String iban;

    public AccountNumberEntity() {}

    public AccountNumberEntity(String iban) {
        this.iban = iban;
    }

    public static AccountNumberEntity toAccountNumberEntity(String iban) {
        return new AccountNumberEntity(iban);
    }
}
