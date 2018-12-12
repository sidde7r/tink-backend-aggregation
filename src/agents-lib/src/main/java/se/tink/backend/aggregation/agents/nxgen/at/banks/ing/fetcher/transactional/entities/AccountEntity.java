package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.transactional.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String username;
    private String iban;
    private String accountType;
    private String currency;
    private String balance;

    private AccountEntity() {
    }

    public AccountEntity(final String username, final String iban, final String accountType, final String currency, final String balance) {
        this.username = username;
        this.iban = iban;
        this.accountType = accountType;
        this.currency = currency;
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public String getIban() {
        return iban;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBalance() {
        return balance;
    }
}
