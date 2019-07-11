package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums;

import se.tink.libraries.payment.enums.PaymentType;

public enum BerlinGroupPaymentType {
    SEPA_CREDIT_TRANSFERS("sepa-credit-transfers", PaymentType.SEPA),
    INSTANT_SEPA_CREDIT_TRANSFERS("instant-sepa-credit-transfers", PaymentType.SEPA),
    Target2Payments(
            "target-2-payments", PaymentType.UNDEFINED), // Can be either domestic or international
    CROSS_BORDER_CREDIT_TRANSFERS("cross-border-credit-transfers", PaymentType.INTERNATIONAL),
    UNDEFINED("Undefind", PaymentType.UNDEFINED);

    BerlinGroupPaymentType(String text, PaymentType paymentType) {
        this.text = text;
        this.paymentType = paymentType;
    }

    private String text;
    private PaymentType paymentType;

    @Override
    public String toString() {
        return this.text;
    }

    public PaymentType getTinkPaymentType() {
        return paymentType;
    }
}
