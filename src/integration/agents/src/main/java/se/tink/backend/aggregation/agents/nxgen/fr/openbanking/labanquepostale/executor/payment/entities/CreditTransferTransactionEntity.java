package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class CreditTransferTransactionEntity {
    private PaymentId paymentId;
    private String requestedExecutionDate;
    private InstructedAmountEntity instructedAmount;
    private List<String> remittanceInformation;

    public static List<CreditTransferTransactionEntity> of(PaymentRequest paymentRequest) {
        InstructedAmountEntity instructedAmount = InstructedAmountEntity.of(paymentRequest);
        ArrayList<CreditTransferTransactionEntity> transactions = new ArrayList<>();
        transactions.add(
                new CreditTransferTransactionEntity.Builder()
                        .withInstructedAmount(instructedAmount)
                        .withRequestedExecutionDate(
                                paymentRequest.getPayment().getExecutionDate().toString())
                        .build());
        return transactions;
    }

    public CreditTransferTransactionEntity() {}

    private CreditTransferTransactionEntity(Builder builder) {
        this.paymentId = builder.paymentId;
        this.requestedExecutionDate = builder.requestedExecutionDate;
        this.instructedAmount = builder.instructedAmount;
        this.remittanceInformation = builder.remittanceInformation;
    }

    public String getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public InstructedAmountEntity getInstructedAmount() {
        return instructedAmount;
    }

    public static class Builder {
        private PaymentId paymentId;
        private String requestedExecutionDate;
        private InstructedAmountEntity instructedAmount;
        private List<String> remittanceInformation;

        public Builder withPaymentId(PaymentId paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder withRequestedExecutionDate(String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public Builder withInstructedAmount(InstructedAmountEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public Builder withRemittanceInformation(List<String> remittanceInformation) {
            this.remittanceInformation = remittanceInformation;
            return this;
        }

        public CreditTransferTransactionEntity build() {
            return new CreditTransferTransactionEntity(this);
        }
    }
}
