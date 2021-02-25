package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class InstructedAmountEntity {
    private String currency;

    private String amount;

    public static InstructedAmountEntity of(PaymentRequest paymentRequest) {
        return new Builder()
                .withAmount(
                        String.valueOf(
                                paymentRequest
                                        .getPayment()
                                        .getExactCurrencyAmount()
                                        .getDoubleValue()))
                .withCurrency(
                        paymentRequest.getPayment().getExactCurrencyAmount().getCurrencyCode())
                .build();
    }

    public InstructedAmountEntity() {}

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }

    private InstructedAmountEntity(Builder builder) {
        this.currency = builder.currency;
        this.amount = builder.amount;
    }

    public static class Builder {
        private String currency;
        private String amount;

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder withAmount(String amount) {
            this.amount = amount;
            return this;
        }

        public InstructedAmountEntity build() {
            return new InstructedAmountEntity(this);
        }
    }
}
