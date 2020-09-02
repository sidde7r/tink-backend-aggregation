package se.tink.libraries.signableoperation.enums;

public enum InternalStatus {
    DUPLICATE_PAYMENT,
    DESTINATION_MESSAGE_TOO_LONG,
    SOURCE_MESSAGE_TOO_LONG,
    BANKID_CANCELLED,
    BANKID_NO_RESPONSE,
    INVALID_OCR,
    INVALID_DESTINATION_MESSAGE,
    INVALID_MINIMUM_AMOUNT,
    INVALID_SOURCE,
    INVALID_DESTINATION,
    DESTINATION_CANT_BE_SAME_AS_SOURCE;
}
