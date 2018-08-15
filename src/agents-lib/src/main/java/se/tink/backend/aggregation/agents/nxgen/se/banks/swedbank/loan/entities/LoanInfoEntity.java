package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanInfoEntity {
    private String name;
    private AccountEntity account;
    private AccountEntity accountForPeyment;

    public String getName() {
        return name;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public AccountEntity getAccountForPeyment() {
        return accountForPeyment;
    }
}
