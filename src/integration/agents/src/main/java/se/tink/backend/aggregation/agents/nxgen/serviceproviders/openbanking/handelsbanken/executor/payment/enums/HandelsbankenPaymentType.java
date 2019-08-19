package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums;

import se.tink.libraries.payment.enums.PaymentType;

public enum HandelsbankenPaymentType {
    SWEDISH_DOMESTICGIRO_PAYMENT("swedish-domestic-giro-payment", PaymentType.DOMESTIC),
    SWEDISH_DOMESTIC_CREDIT_TRANSFER("swedish-domestic-credit-transfer", PaymentType.DOMESTIC),
    SEPA_CREDIT_TRANSFER("sepa-credit-transfer", PaymentType.SEPA),
    CROSS_CURRENCY_CREDIT_TRANSFER("cross-currency-credit-transfer", PaymentType.INTERNATIONAL),
    BRITISH_DOMESTIC_CREDIT_TRANSFER("british-domestic-credit-transfer", PaymentType.DOMESTIC),
    UNDEFINED("Undefined", PaymentType.UNDEFINED);

    private final String text;
    private final PaymentType paymentType;

    HandelsbankenPaymentType(String text, PaymentType paymentType) {
        this.text = text;
        this.paymentType = paymentType;
    }

    @Override
    public String toString() {
        return text;
    }

    public PaymentType getTinkPaymentType() {
        return paymentType;
    }
}
