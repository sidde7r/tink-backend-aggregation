package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities;

import net.minidev.json.annotate.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class AmountEntity {
    private String currency;
    private String amount;
    private String equivalentAmount;

    @JsonIgnore
    private AmountEntity(Builder builder) {
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.equivalentAmount = builder.equivalentAmount;
    }

    public AmountEntity() {}

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }

    @JsonIgnore
    public static AmountEntity of(PaymentRequest paymentRequest) {
        return new AmountEntity.Builder()
                .withCurrency(paymentRequest.getPayment().getAmount().getCurrency())
                .withAmount(paymentRequest.getPayment().getAmount().getValue().toString())
                .build();
    }

    public static class Builder {
        private String currency;
        private String amount;
        private String equivalentAmount;

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder withAmount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder withEquivalentAmount(String equivalentAmount) {
            this.equivalentAmount = equivalentAmount;
            return this;
        }

        public AmountEntity build() {
            return new AmountEntity(this);
        }
    }
}
