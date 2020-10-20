package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class InstructedAmountEntity {
    private String currency;
    private BigDecimal amount;

    public static InstructedAmountEntity of(PaymentRequest paymentRequest) {
        return new Builder()
                .withAmount(paymentRequest.getPayment().getExactCurrencyAmount().getExactValue())
                .withCurrency(paymentRequest.getPayment().getCurrency())
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

    public BigDecimal getAmount() {
        return amount;
    }

    public static class Builder {
        private String currency;
        private BigDecimal amount;

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder withAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public InstructedAmountEntity build() {
            return new InstructedAmountEntity(this);
        }
    }
}
