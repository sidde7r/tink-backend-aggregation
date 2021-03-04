package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.enums.RaiffeisenPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class GetPaymentResponse {
    private String uniqueID;
    private String endToEndIdentification;
    private AccountEntity debtorAccount;
    private InstructedAmountEntity instructedAmount;
    private AccountEntity creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private String creditorAddress;
    private String remittanceInformationUnstructured;
    private String transactionStatus;

    public GetPaymentResponse() {}

    public PaymentResponse toTinkPaymentResponse(PaymentType sepa) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withStatus(
                                RaiffeisenPaymentStatus.mapToTinkPaymentStatus(
                                        RaiffeisenPaymentStatus.fromString(transactionStatus)))
                        .withExactCurrencyAmount(
                                ExactCurrencyAmount.of(
                                        instructedAmount.getAmount(),
                                        instructedAmount.getCurrency()))
                        .withCurrency(instructedAmount.getCurrency())
                        .withUniqueId(uniqueID);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    private GetPaymentResponse(Builder builder) {
        this.endToEndIdentification = builder.endToEndIdentification;
        this.debtorAccount = builder.debtorAccount;
        this.instructedAmount = builder.instructedAmount;
        this.creditorAccount = builder.creditorAccount;
        this.creditorAgent = builder.creditorAgent;
        this.creditorName = builder.creditorName;
        this.remittanceInformationUnstructured = builder.remittanceInformationUnstructured;
        this.creditorAddress = builder.creditorAddress;
        this.transactionStatus = builder.transactionStatus;
    }

    public static class Builder {
        private String endToEndIdentification;
        private AccountEntity debtorAccount;
        private InstructedAmountEntity instructedAmount;
        private AccountEntity creditorAccount;
        private String creditorAgent;
        private String creditorName;
        private String creditorAddress;
        private String remittanceInformationUnstructured;
        private String transactionStatus;

        public Builder withEndToEndIdentification(String endToEndIdentification) {
            this.endToEndIdentification = endToEndIdentification;
            return this;
        }

        public Builder withDebtorAccount(AccountEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public Builder withInstructedAmount(InstructedAmountEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public Builder withCreditorAccount(AccountEntity creditorAccount) {
            this.creditorAccount = creditorAccount;
            return this;
        }

        public Builder withCreditorAgent(String creditorAgent) {
            this.creditorAgent = creditorAgent;
            return this;
        }

        public Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public Builder withCreditorAddress(String creditorAddress) {
            this.creditorAddress = creditorAddress;
            return this;
        }

        public Builder withRemittanceInformationUnstructured(
                String remittanceInformationUnstructured) {
            this.remittanceInformationUnstructured = remittanceInformationUnstructured;
            return this;
        }

        public Builder withRtransactionStatus(String transactionStatus) {
            this.transactionStatus = transactionStatus;
            return this;
        }

        public GetPaymentResponse build() {
            return new GetPaymentResponse(this);
        }
    }
}
