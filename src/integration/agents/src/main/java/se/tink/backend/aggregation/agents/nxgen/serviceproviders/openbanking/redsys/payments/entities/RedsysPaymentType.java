package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities;

import se.tink.libraries.payments.common.model.PaymentScheme;

public enum RedsysPaymentType {
    SEPA_CREDIT_TRANSFERS("sepa-credit-transfers"),
    INSTANT_SEPA_CREDIT_TRANSFERS("instant-sepa-credit-transfers");

    private String value;

    RedsysPaymentType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static String fromTinkPaymentType(PaymentScheme paymentScheme) {
        return PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER.equals(paymentScheme)
                ? INSTANT_SEPA_CREDIT_TRANSFERS.toString()
                : SEPA_CREDIT_TRANSFERS.toString();
    }
}
