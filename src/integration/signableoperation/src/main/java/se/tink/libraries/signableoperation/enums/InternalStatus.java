package se.tink.libraries.signableoperation.enums;

public enum InternalStatus {
    DUPLICATE_PAYMENT,
    EXISTING_UNSIGNED_TRANSFERS,
    TRANSFER_LIMIT_REACHED,

    // Payment Data
    INVALID_DUE_DATE,
    SOURCE_MESSAGE_TOO_LONG,
    INVALID_OCR,
    DESTINATION_MESSAGE_TOO_LONG,
    INVALID_DESTINATION_MESSAGE,
    INVALID_DESTINATION_MESSAGE_TYPE,
    INVALID_MINIMUM_AMOUNT,
    INVALID_MAXIMUM_AMOUNT,
    INSUFFICIENT_FUNDS,
    NEW_RECIPIENT_NAME_ABSENT,
    INVALID_PAYMENT_TYPE,
    PAYMENT_VALIDATION_FAILED_NO_DESCRIPTION,

    // BankID specific
    BANKID_CANCELLED,
    BANKID_NO_RESPONSE,
    BANKID_ANOTHER_IN_PROGRESS,
    BANKID_NEEDS_EXTENDED_USE_ENABLED,
    BANKID_TIMEOUT,
    BANKID_INTERRUPTED,
    BANKID_UNKNOWN_EXCEPTION,

    // Security related
    INVALID_GRANT,
    INVALID_SECURITY_TOKEN,
    SECURITY_TOKEN_NO_RESPONSE,
    USER_REQUIRES_TRANSFER_PERMISSION,
    USER_UNAUTHORIZED,
    PAYMENT_SIGNATURE_FAILED,
    INVALID_CLAIM_ERROR,

    // Account specific
    INVALID_SOURCE_ACCOUNT,
    INVALID_DESTINATION_ACCOUNT,
    INVALID_ACCOUNT_TYPE_COMBINATION,
    DESTINATION_CANT_BE_SAME_AS_SOURCE,
    ACCOUNT_BLOCKED_FOR_TRANSFER,
    COULD_NOT_SAVE_NEW_RECIPIENT,
    UNREGISTERED_RECIPIENT,

    // User Authorization errors
    PAYMENT_AUTHORIZATION_CANCELLED,
    PAYMENT_AUTHORIZATION_TIMEOUT,
    PAYMENT_AUTHORIZATION_FAILED,
    PAYMENT_AUTHORIZATION_UNKNOWN_EXCEPTION,

    // Bank Side errors
    BANK_SERVICE_UNAVAILABLE,
    BANK_CONNECTION_INTERRUPTED,
    PAYMENT_REJECTED_BY_BANK_NO_DESCRIPTION,

    // Error we should handle
    BANK_ERROR_CODE_NOT_HANDLED_YET
}
