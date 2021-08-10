package se.tink.backend.aggregation.agents.consent.generators.nl.triodos;

import se.tink.backend.aggregation.agents.consent.Scope;

public enum TriodosScope implements Scope {
    ACCOUNTS("accounts"),
    BALANCES("balances"),
    TRANSACTIONS("transactions");

    private final String value;

    TriodosScope(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
