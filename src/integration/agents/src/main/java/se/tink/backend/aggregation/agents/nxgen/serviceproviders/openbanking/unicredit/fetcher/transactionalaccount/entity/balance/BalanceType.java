package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.balance;

public enum BalanceType {
    AUTHORISED("authorised", 3),
    EXPECTED("expected", 1),
    CLOSING_BOOKED("closingBooked", 2);

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
