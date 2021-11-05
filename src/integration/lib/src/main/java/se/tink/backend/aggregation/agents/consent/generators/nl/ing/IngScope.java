package se.tink.backend.aggregation.agents.consent.generators.nl.ing;

import se.tink.backend.aggregation.agents.consent.Scope;

public enum IngScope implements Scope {
    VIEW_PAYMENT_BALANCES("payment-accounts:balances:view"),
    VIEW_PAYMENT_TRANSACTIONS("payment-accounts:transactions:view");

    private final String value;

    IngScope(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
