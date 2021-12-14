package se.tink.libraries.transfer.enums;

public enum RemittanceInformationType {
    UNSTRUCTURED,
    OCR,
    REFERENCE,
    // RF is Finland-specific Creditor Reference specified in ISO 11649:2009, reference number to
    // document payment is for.
    RF,
    INVOICE,
    // KID (kundeidentifikasjon / customer identification) - NO market only, reference to a
    // customer number
    KID
}
