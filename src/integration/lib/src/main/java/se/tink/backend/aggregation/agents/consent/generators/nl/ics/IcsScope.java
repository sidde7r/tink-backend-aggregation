package se.tink.backend.aggregation.agents.consent.generators.nl.ics;

import se.tink.backend.aggregation.agents.consent.Scope;

public enum IcsScope implements Scope {
    READ_ACCOUNT_BASIC("ReadAccountsBasic"),
    READ_ACCOUNTS_DETAIL("ReadAccountsDetail"),
    READ_BALANCES("ReadBalances"),
    READ_TRANSACTION_BASIC("ReadTransactionsBasic"),
    READ_TRANSACTIONS_CREDITS("ReadTransactionsCredits"),
    READ_TRANSACTIONS_DEBITS("ReadTransactionsDebits"),
    READ_TRANSACTIONS_DETAIL("ReadTransactionsDetail");

    private final String value;

    IcsScope(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
