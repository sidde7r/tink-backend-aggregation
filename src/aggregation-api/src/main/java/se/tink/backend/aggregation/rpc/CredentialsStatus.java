package se.tink.backend.aggregation.rpc;

public enum CredentialsStatus {
    CREATED,
    AUTHENTICATING,
    UNCHANGED,
    UPDATING,
    UPDATED,
    AWAITING_MOBILE_BANKID_AUTHENTICATION,
    AWAITING_THIRD_PARTY_APP_AUTHENTICATION,
    AWAITING_SUPPLEMENTAL_INFORMATION,
    AWAITING_OTHER_CREDENTIALS_TYPE,
    AUTHENTICATION_ERROR,
    NOT_IMPLEMENTED_ERROR,
    TEMPORARY_ERROR,
    HINTED,
    PERMANENT_ERROR,
    DELETED,
    METRIC,
    DISABLED
}
