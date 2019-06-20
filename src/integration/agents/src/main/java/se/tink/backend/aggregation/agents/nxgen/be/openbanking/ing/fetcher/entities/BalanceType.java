package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;

public enum BalanceType {
    EXPECTED(IngConstants.BalanceTypes.EXPECTED, 1),
    CLOSING_BOOKED(IngConstants.BalanceTypes.CLOSING_BOOKED, 2),
    INTERIM_BOOKED(IngConstants.BalanceTypes.INTERIM_BOOKED, 3);

    private final String value;
    private final int priority;

    BalanceType(String value, int priority) {
        this.value = value;
        this.priority = priority;
    }

    public String getValue() {
        return value;
    }

    public int getPriority() {
        return priority;
    }
}
