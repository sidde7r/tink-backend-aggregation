package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.rpc;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.entity.PaymentRecipient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class ExecutePaymentRequest {

    private String externalIdentifier;
    private PaymentRecipient paymentRecipient;
    private String reference;
    private AmountEntity amount;

    private ExecutePaymentRequest(Builder builder) {
        this.externalIdentifier = builder.getExternalIdentifier();
        this.paymentRecipient = builder.getPaymentRecipient();
        this.reference = builder.getReference();
        this.amount = builder.getAmount();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String externalIdentifier;
        private PaymentRecipient paymentRecipient;
        private String reference;
        private AmountEntity amount;

        public Builder setExternalIdentifier(String externalIdentifier) {
            Preconditions.checkNotNull(externalIdentifier);

            this.externalIdentifier = externalIdentifier;
            return this;
        }

        public Builder setPaymentRecipient(PaymentRecipient paymentRecipient) {
            Preconditions.checkNotNull(paymentRecipient);

            this.paymentRecipient = paymentRecipient;
            return this;
        }

        public Builder setReference(String reference) {
            Preconditions.checkNotNull(reference);

            this.reference = reference;
            return this;
        }

        public Builder setAmount(Amount amount) {
            Preconditions.checkNotNull(amount);

            this.amount = AmountEntity.fromAmount(amount);
            return this;
        }

        public ExecutePaymentRequest build() {

            return new ExecutePaymentRequest(this);
        }

        String getExternalIdentifier() {
            return externalIdentifier;
        }

        PaymentRecipient getPaymentRecipient() {
            return paymentRecipient;
        }

        String getReference() {
            return reference;
        }

        AmountEntity getAmount() {
            return amount;
        }
    }
}
