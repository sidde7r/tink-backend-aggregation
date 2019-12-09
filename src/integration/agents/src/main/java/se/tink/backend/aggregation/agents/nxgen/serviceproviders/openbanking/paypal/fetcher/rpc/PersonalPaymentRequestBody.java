package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.RequestConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.payment.PayeeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class PersonalPaymentRequestBody {
    private AmountEntity amount;
    private PayeeEntity payee;

    @JsonProperty("payment_type")
    private String paymentType;

    public PersonalPaymentRequestBody() {}

    private PersonalPaymentRequestBody(Builder builder) {
        this.amount = builder.amount;
        this.payee = builder.payee;
        this.paymentType = builder.paymentType;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public PayeeEntity getPayee() {
        return payee;
    }

    public String getPaymentType() {
        return paymentType;
    }

    @JsonIgnore
    public static PersonalPaymentRequestBody of(PaymentRequest paymentRequest) {
        AmountEntity amount = AmountEntity.of(paymentRequest);
        PayeeEntity payee = PayeeEntity.of(paymentRequest);
        String paymentType = RequestConstants.PERSONAL;
        return new Builder()
                .withAmount(amount)
                .withPayee(payee)
                .withPaymentType(paymentType)
                .build();
    }

    public static class Builder {
        private AmountEntity amount;
        private PayeeEntity payee;
        private String paymentType;

        public Builder withAmount(AmountEntity amount) {
            this.amount = amount;
            return this;
        }

        public Builder withPayee(PayeeEntity payee) {
            this.payee = payee;
            return this;
        }

        public Builder withPaymentType(String paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public PersonalPaymentRequestBody build() {
            return new PersonalPaymentRequestBody(this);
        }
    }
}
