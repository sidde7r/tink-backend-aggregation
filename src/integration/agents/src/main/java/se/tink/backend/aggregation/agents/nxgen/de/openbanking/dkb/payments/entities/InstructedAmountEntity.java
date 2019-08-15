package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.Amount;

@JsonObject
public class InstructedAmountEntity {
    private String currency;
    private String amount;

    public InstructedAmountEntity() {}

    public String getCurrency() {
        return currency;
    }

    public Double getAmount() {
        return Double.parseDouble(amount);
    }

    @JsonIgnore
    private InstructedAmountEntity(Builder builder) {
        this.currency = builder.currency;
        this.amount = builder.amount;
    }

    @JsonIgnore
    public static InstructedAmountEntity of(PaymentRequest paymentRequest) {
        Amount amount = paymentRequest.getPayment().getAmount();
        return new InstructedAmountEntity.Builder()
                .withAmount(amount.getValue().toString())
                .withCurrency(amount.getCurrency())
                .build();
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
