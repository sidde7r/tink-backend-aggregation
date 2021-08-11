package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.nordea;

import se.tink.backend.aggregation.agents.consent.Scope;

public enum NordeaScope implements Scope {
    READ_ACCOUNT_BASIC("ACCOUNTS_BASIC"),
    READ_ACCOUNT_BALANCES("ACCOUNTS_BALANCES"),
    READ_ACCOUNTS_DETAIL("ACCOUNTS_DETAILS"),
    READ_ACCOUNTS_TRANSACTIONS("ACCOUNTS_TRANSACTIONS"),
    READ_CARDS_INFORMATION("CARDS_INFORMATION"),
    READ_CARDS_TRANSACTIONS("CARDS_TRANSACTIONS"),
    PAYMENTS_MULTIPLE("PAYMENTS_MULTIPLE");

    private final String value;

    NordeaScope(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
