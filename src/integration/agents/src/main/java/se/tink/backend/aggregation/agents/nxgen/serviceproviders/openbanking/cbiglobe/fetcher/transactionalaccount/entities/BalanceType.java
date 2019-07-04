package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

public enum BalanceType {
    EXPECTED("interimAvailable", 1),
    CLOSING_BOOKED("forwardAvailable", 2),
    INTERIM_BOOKED("expected", 3);

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
