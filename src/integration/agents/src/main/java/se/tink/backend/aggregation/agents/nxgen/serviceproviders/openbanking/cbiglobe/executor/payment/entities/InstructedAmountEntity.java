package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class InstructedAmountEntity {

    private String currency;

    private String amount;

    @JsonIgnore
    public static InstructedAmountEntity of(PaymentRequest paymentRequest) {
        return new Builder()
                .withAmount(paymentRequest.getPayment().getAmount().getValue().toString())
                .withCurrency(paymentRequest.getPayment().getAmount().getCurrency())
                .build();
    }

    public InstructedAmountEntity() {}

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }

    @JsonIgnore
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
