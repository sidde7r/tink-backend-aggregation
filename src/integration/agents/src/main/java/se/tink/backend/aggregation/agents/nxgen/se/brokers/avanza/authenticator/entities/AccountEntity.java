package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String accountName;
    private String accountType;

    public String getAccountName() {
        return accountName;
    }

    public String getAccountType() {
        return accountType;
    }
}
