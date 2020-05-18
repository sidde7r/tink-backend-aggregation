package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class InstructedAmountEntity {
    private String currency;
    private double amount;

    public static InstructedAmountEntity of(PaymentRequest paymentRequest) {
        return new Builder()
                .withAmount(paymentRequest.getPayment().getAmount().getValue())
                .withCurrency(paymentRequest.getPayment().getAmount().getCurrency())
                .build();
    }

    public InstructedAmountEntity() {}

    private InstructedAmountEntity(Builder builder) {
        this.currency = builder.currency;
        this.amount = builder.amount;
    }

    public String getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public static class Builder {
        private String currency;
        private double amount;

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder withAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public InstructedAmountEntity build() {
            return new InstructedAmountEntity(this);
        }
    }
}
