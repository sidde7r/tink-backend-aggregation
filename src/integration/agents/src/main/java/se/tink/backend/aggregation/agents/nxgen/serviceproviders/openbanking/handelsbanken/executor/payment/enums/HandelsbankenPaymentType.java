package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums;

import se.tink.libraries.payment.enums.PaymentType;

public enum HandelsbankenPaymentType {
    SwedishDomesticGiroPayment("swedish-domestic-giro-payment", PaymentType.DOMESTIC),
    SwedishDomesticCreditTransfer("swedish-domestic-credit-transfer", PaymentType.DOMESTIC),
    SepaCreditTransfer("sepa-credit-transfer", PaymentType.SEPA),
    CrossCurrencyCreditTransfer("cross-currency-credit-transfer", PaymentType.INTERNATIONAL),
    BritishDomesticCreditTransfer("british-domestic-credit-transfer", PaymentType.DOMESTIC),
    Undefined("Undefined", PaymentType.UNDEFINED);

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
