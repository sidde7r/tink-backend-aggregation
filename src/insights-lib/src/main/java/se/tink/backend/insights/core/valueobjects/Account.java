package se.tink.backend.insights.core.valueobjects;

import se.tink.backend.core.AccountTypes;

public class Account {
    private String name;
    private AccountId accountId;
    private Balance balance;
    // TODO: make insight account type
    private AccountTypes type;

    Account(String name, AccountId accountId, Balance balance, AccountTypes type) {
        this.name = name;
        this.accountId = accountId;
        this.balance = balance;
        this.type = type;
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public Balance getBalance() {
        return balance;
    }

    public AccountTypes getType() {
        return type;
    }

    public static Account of(String name, AccountId accountId, Balance balance, AccountTypes type) {
        return new Account(name, accountId, balance, type);
    }

    public String getName() {
        return name;
    }
}
