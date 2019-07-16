package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class AmountEntity {

    private String value;

    @JsonProperty("currency_code")
    private String currencyCode;

    public AmountEntity() {}

    private AmountEntity(Builder builder) {
        this.value = builder.value;
        this.currencyCode = builder.currencyCode;
    }

    public String getValue() {
        return value;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    @JsonIgnore
    public static AmountEntity of(PaymentRequest paymentRequest) {
        return new Builder()
                .withCurrency(paymentRequest.getPayment().getAmount().getCurrency())
                .withValue(paymentRequest.getPayment().getAmount().getValue().toString())
                .build();
    }

    public static class Builder {
        private String value;

        private String currencyCode;

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withCurrency(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public AmountEntity build() {
            return new AmountEntity(this);
        }
    }
}
