package se.tink.backend.aggregation.agents.consent.generators.fi.opbank;

import se.tink.backend.aggregation.agents.consent.Scope;

public enum OpBankScope implements Scope {
    OPENID("openid"),
    VERIFY_ACCOUNTS("accounts:verify"),
    READ_ACCOUNTS("accounts"),
    READ_ACCOUNTS_TRANSACTIONS("accounts:transactions"),
    READ_ACCOUNTS_TRANSACTIONS_HISTORY("accounts:transactions-history"),
    READ_CARDS("cards"),
    READ_CARDS_TRANSACTIONS("cards:transactions"),
    READ_CARDS_TRANSACTIONS_HISTORY("cards:transactions-history");

    private final String value;

    OpBankScope(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
