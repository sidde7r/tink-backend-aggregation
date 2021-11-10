package se.tink.backend.agents.rpc;

// These types are following ISO 20022 standards.
// They are copied from UkOpenBankingApiDefinitions.AccountBalanceType to avoid coupling
public enum AccountBalanceType {
    CLEARED_BALANCE,
    CLOSING_AVAILABLE,
    CLOSING_BOOKED,
    CLOSING_CLEARED,
    EXPECTED,
    FORWARD_AVAILABLE,
    INFORMATION,
    INTERIM_AVAILABLE,
    INTERIM_BOOKED,
    INTERIM_CLEARED,
    OPENING_AVAILABLE,
    OPENING_BOOKED,
    OPENING_CLEARED,
    PREVIOUSLY_CLOSED_BOOKED;
}
