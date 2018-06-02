package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBalanceEntity {
    private Double balance;
    private Double available;

    public Double getBalance() {
        return balance;
    }

    public Double getAvailable() {
        return available;
    }
}
