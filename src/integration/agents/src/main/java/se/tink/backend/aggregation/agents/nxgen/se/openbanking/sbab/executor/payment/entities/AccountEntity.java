package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String accountNumber;

    public AccountEntity() {}

    public AccountEntity(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIdentifier() {
        return accountNumber;
    }
}
