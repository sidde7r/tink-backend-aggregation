package se.tink.libraries.signableoperation.enums;

public enum InternalStatus {
    DUPLICATE_PAYMENT,
    DESTINATION_MESSAGE_TOO_LONG,
    SOURCE_MESSAGE_TOO_LONG,
    BANKID_CANCELLED,
    BANKID_NO_RESPONSE,
    BANKID_ANOTHER_IN_PROGRESS,
    SECURITY_TOKEN_NO_RESPONSE,
    INVALID_OCR,
    INVALID_DESTINATION_MESSAGE,
    INVALID_MINIMUM_AMOUNT,
    INVALID_SOURCE_ACCOUNT,
    INVALID_DESTINATION_ACCOUNT,
    DESTINATION_CANT_BE_SAME_AS_SOURCE,
    INSUFFICIENT_FUNDS,
    INVALID_DUE_DATE,
    EXISTING_UNSIGNED_TRANSFERS,
    BANK_ERROR_CODE_NOT_HANDLED_YET,
    TRANSFER_LIMIT_REACHED,
    ACCOUNT_BLOCKED_FOR_TRANSFER,
    USER_REQUIRES_TRANSFER_PERMISSION,
    INVALID_DESTINATION_MESSAGE_TYPE
}
