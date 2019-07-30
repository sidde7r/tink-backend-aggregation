package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.enums;

public enum UnicreditPaymentProduct {
    SEPA_CREDIT_TRANSFERS("sepa-credit-transfers"),
    INSTANT_SEPA_CREDIT_TRANSFERS("instant-sepa-credit-transfers"),
    TARGET_2_PAYMENTS("target-2-payments"),
    CROSS_BORDER_CREDIT_TRANSFERS("cross-border-credit-transfers");

    private String value;

    UnicreditPaymentProduct(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
