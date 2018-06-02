package se.tink.backend.core.auth.bankid;

public enum BankIdAuthenticationStatus {
    AUTHENTICATED,
    AUTHENTICATION_ERROR,
    AWAITING_BANKID_AUTHENTICATION,
    NO_USER
}
