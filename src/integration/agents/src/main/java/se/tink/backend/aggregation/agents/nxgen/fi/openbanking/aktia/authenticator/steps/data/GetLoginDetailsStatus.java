package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.data;

public enum GetLoginDetailsStatus {
    LOGGED_IN,
    OTP_REQUIRED,
    ERROR_IN_RESPONSE,
    PASSWORD_CHANGE_REQUIRED,
    ACCOUNT_LOCKED,
    MUST_ACCEPT_TERMS
}
