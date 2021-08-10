package se.tink.backend.aggregation.agents.consent.generators.se.swedbank;

import se.tink.backend.aggregation.agents.consent.Scope;

public enum SwedbankScope implements Scope {
    PSD2("PSD2"),
    READ_ACCOUNTS_BALANCES("PSD2account_balances"),
    READ_TRANSACTIONS_HISTORY("PSD2account_transactions"),
    READ_TRANSACTIONS_HISTORY_OVER90("PSD2account_transactions_over90");

    private final String value;

    SwedbankScope(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
