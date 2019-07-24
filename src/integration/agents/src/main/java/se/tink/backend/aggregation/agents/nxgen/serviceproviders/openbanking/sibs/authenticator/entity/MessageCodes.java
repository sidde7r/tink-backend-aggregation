package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity;

public enum MessageCodes {
    SERVICE_BLOCKED,
    CORPORATE_ID_IVALID, // Typo is present in SIBS documentation
    CONSENT_UNKNOWN,
    CONSENT_INVALID,
    CONSENT_EXPIRED,
    RESOURCE_UNIKNOWN, // Typo is present in SIBS documentation
    RESOURCE_EXPIRED,
    TIMESTAMP_INVALID,
    PERIOD_INVALID,
    SCA_METHOD_UNKKNOWN, // Typo is present in SIBS documentation
    TRANSACTION_ID_INVALID,
    PRODUCT_INVALID,
    PRODUCT_UNKNOWN,
    PAYMENT_FAILED,
    REQUIRED_KID_MISSING,
    SESSIONS_NOT_SUPPORTED,
    ACCESS_EXCEEDED,
    REQUESTED_FORMATS_INVALID,
    CARD_INVALID,
    NO_PIIS_ACTIVATION
}
