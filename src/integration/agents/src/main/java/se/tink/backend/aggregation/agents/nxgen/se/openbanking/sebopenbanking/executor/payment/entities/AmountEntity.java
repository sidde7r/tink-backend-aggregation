package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
@JsonInclude(Include.NON_NULL)
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
    public static AmountEntity of(PaymentRequest paymentRequest) throws PaymentValidationException {
        BigDecimal amount = paymentRequest.getPayment().getExactCurrencyAmount().getExactValue();
        if (BigDecimal.ONE.compareTo(amount) > 0) {
            throw PaymentValidationException.invalidMinimumAmount();
        } else {
            return new AmountEntity.Builder()
                    .withCurrency(
                            paymentRequest.getPayment().getExactCurrencyAmount().getCurrencyCode())
                    .withAmount(amount.toString())
                    .build();
        }
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

        public AmountEntity build() {
            return new AmountEntity(this);
        }
    }
}
