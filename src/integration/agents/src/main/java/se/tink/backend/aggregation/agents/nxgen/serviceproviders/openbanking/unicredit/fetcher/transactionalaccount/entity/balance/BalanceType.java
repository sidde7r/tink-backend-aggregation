package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.balance;

public enum BalanceType {
    AUTHORISED("authorised", 3),
    EXPECTED("expected", 1),
    CLOSING_BOOKED("closingBooked", 2),
    INTERIM_AVAILABLE("interimAvailable", 4),
    OPENING_BOOKED("openingBooked", 5),
    INTERIM_BOOKED("interimBooked", 6),
    FORWARD_AVAILABLE("forwardAvailable", 7),
    NON_INVOICED("nonInvoiced", 8);

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
