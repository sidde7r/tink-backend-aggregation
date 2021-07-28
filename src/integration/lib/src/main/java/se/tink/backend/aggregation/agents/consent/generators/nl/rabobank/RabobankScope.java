package se.tink.backend.aggregation.agents.consent.generators.nl.rabobank;

import se.tink.backend.aggregation.agents.consent.Scope;

public enum RabobankScope implements Scope {
    READ_BALANCES("ais.balances.read"),
    READ_TRANSACTIONS_90DAYS("ais.transactions.read-90days"),
    READ_TRANSACTIONS_HISTORY("ais.transactions.read-history");

    private final String value;

    RabobankScope(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
