package se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums;

import se.tink.libraries.transfer.rpc.PaymentServiceType;

public enum PaymentService {
    PERIODIC_PAYMENTS("periodic-payments"),
    PAYMENTS("payments");

    private String value;

    PaymentService(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static String getPaymentService(PaymentServiceType paymentServiceType) {
        return PaymentServiceType.PERIODIC.equals(paymentServiceType)
                ? PERIODIC_PAYMENTS.toString()
                : PAYMENTS.toString();
    }
}
