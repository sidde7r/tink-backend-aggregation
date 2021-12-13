package se.tink.libraries.transfer.enums;

public enum RemittanceInformationType {
    UNSTRUCTURED,
    OCR,
    REFERENCE,
    // RF is Finland-specific reference
    RF,
    INVOICE,
    // KID (kundeidentifikasjon) - NO specific reference
    KID
}
