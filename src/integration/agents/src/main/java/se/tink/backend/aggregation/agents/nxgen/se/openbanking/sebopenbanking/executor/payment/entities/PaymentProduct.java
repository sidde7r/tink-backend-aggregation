package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public enum PaymentProduct {
    SWEDISH_DOMESTIC_PRIVATE_BANKGIROS("swedish-domestic-private-bankgiros"),
    SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS("swedish-domestic-private-plusgiros"),
    SEPA_CREDIT_TRANSFER("sepa-credit-transfers"),
    SWEDISH_DOMESTIC_PRIVATE_CREDIT_TRANSFERS("swedish-domestic-private-credit-transfers");

    private String value;

    @JsonIgnore
    PaymentProduct(String value) {
        this.value = value;
    }

    @JsonIgnore
    public static PaymentProduct fromString(String text) {
        for (PaymentProduct paymentProduct : PaymentProduct.values()) {
            if (paymentProduct.value.equalsIgnoreCase(text)) {
                return paymentProduct;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }
}
