package se.tink.backend.aggregation.agents.consent.generators.nl.abnamro;

import se.tink.backend.aggregation.agents.consent.Scope;

public enum AbnAmroScope implements Scope {
    READ_ACCOUNTS("psd2:account:balance:read"),
    READ_ACCOUNTS_DETAILS("psd2:account:details:read"),
    READ_TRANSACTIONS_HISTORY("psd2:account:transaction:read");

    private final String value;

    AbnAmroScope(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
