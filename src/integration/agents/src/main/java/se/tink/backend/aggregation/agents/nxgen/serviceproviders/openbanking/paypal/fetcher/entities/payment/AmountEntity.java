package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private String value;
    private String currency;

    public AmountEntity() {}

    private AmountEntity(Builder builder) {
        this.value = builder.value;
        this.currency = builder.currency;
    }

    public String getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public static AmountEntity of(PaymentRequest paymentRequest) {
        return new Builder()
                .withCurrency(paymentRequest.getPayment().getAmount().getCurrency())
                .withValue(paymentRequest.getPayment().getAmount().getValue().toString())
                .build();
    }

    public Amount toTinkAmount() {
        return new Amount(currency, Double.valueOf(value));
    }

    public static class Builder {
        private String value;
        private String currency;

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public AmountEntity build() {
            return new AmountEntity(this);
        }
    }
}
