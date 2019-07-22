package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary;

public enum SibsPaymentType {
    SEPA_CREDIT_TRANSFERS("sepa-credit-transfers"),
    CROSS_BORDER_CREDIT_TRANSFERS("cross-border-credit-transfers"),
    INSTANT_SEPA_CREDIT_TRANSFERS("instant-sepa-credit-transfers"),
    TARGET_2_PAYMENTS("target-2-payments");

    private String value;

    SibsPaymentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
