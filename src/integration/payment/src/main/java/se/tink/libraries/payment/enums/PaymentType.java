package se.tink.libraries.payment.enums;

public enum PaymentType {
    UNDEFINED,
    DOMESTIC,
    SEPA,
    INTERNATIONAL,
    DOMESTIC_FUTURE,
    INTERNATIONAL_FUTURE;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
