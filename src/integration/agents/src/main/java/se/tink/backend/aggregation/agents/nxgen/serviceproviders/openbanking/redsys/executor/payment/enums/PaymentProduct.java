package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.enums;

import se.tink.libraries.payment.enums.PaymentType;

public enum PaymentProduct {
    SEPA_TRANSFER("sepa-credit-transfers", PaymentType.SEPA),
    SEPA_INSTANT_TRANSFER("instant-sepa-credit-transfers", PaymentType.SEPA),
    TARGET_2("target-2-payments", PaymentType.SEPA),
    CROSS_BORDER("cross-border-credit-transfers", PaymentType.INTERNATIONAL);

    private String productName;
    private PaymentType paymentType;

    PaymentProduct(String productName, PaymentType paymentType) {
        this.productName = productName;
        this.paymentType = paymentType;
    }

    public String getProductName() {
        return productName;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }
}
