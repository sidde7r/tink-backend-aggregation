package se.tink.libraries.payment.enums;

public enum PaymentType {
    UNDEFINED,
    DOMESTIC,
    SEPA,
    INTERNATIONAL;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
