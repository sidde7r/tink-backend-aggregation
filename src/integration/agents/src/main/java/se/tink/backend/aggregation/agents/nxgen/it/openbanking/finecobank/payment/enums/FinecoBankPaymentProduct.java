package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums;

public enum FinecoBankPaymentProduct {
    SEPA_CREDIT_TRANSFER("sepa-credit-transfers"),
    CROSS_BORDER_CREDIT_TRANSFER("cross-border-credit-transfers");

    private String value;

    FinecoBankPaymentProduct(String paymentProduct) {
        this.value = paymentProduct;
    }

    public String getValue() {
        return value;
    }
}
