package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class PurchaseUnitsEntity {
    private AmountEntity amount;
    private PayeeEntity payee;

    public PurchaseUnitsEntity() {}

    private PurchaseUnitsEntity(Builder builder) {
        this.amount = builder.amount;
        this.payee = builder.payee;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public PayeeEntity getPayee() {
        return payee;
    }

    @JsonIgnore
    public static PurchaseUnitsEntity of(PaymentRequest paymentRequest) {
        return new Builder()
                .withAmount(AmountEntity.of(paymentRequest))
                .withPayee(PayeeEntity.of(paymentRequest))
                .build();
    }

    public static class Builder {
        private AmountEntity amount;
        private PayeeEntity payee;

        public Builder withAmount(AmountEntity amount) {
            this.amount = amount;
            return this;
        }

        public Builder withPayee(PayeeEntity payee) {
            this.payee = payee;
            return this;
        }

        public PurchaseUnitsEntity build() {
            return new PurchaseUnitsEntity(this);
        }
    }
}
