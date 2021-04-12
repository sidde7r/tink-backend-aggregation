package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanInfoEntity {
    private String name;
    private AccountEntity account;
    private AccountEntity accountForPayment;

    public String getName() {
        return name;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public AccountEntity getAccountForPayment() {
        return accountForPayment;
    }
}
