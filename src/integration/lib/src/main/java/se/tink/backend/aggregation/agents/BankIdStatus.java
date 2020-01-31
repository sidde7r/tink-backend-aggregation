package se.tink.backend.aggregation.agents;

public enum BankIdStatus {
    DONE,
    CANCELLED,
    FAILED_UNKNOWN,
    TIMEOUT,
    WAITING,
    INTERRUPTED,
    EXPIRED_AUTOSTART_TOKEN,
    NO_CLIENT
}
