package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {
    private AccountEntity debtorAccount;
    private InstructedAmountEntity instructedAmount;
    private AccountEntity creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private String creditorAddress;
    private String chargeBearer;
    private String remittanceInformationUnsecured;
    private String requestedExecutionDate;
    private String phoneNumber;
    private String email;
    private String transactionText;
    private String paymentReason;
    private String priority;

    public CreatePaymentRequest(Builder builder) {
        this.debtorAccount = builder.debtorAccount;
        this.instructedAmount = builder.instructedAmount;
        this.creditorAccount = builder.creditorAccount;
        this.creditorAgent = builder.creditorAgent;
        this.creditorName = builder.creditorName;
        this.creditorAddress = builder.creditorAddress;
        this.chargeBearer = builder.chargeBearer;
        this.remittanceInformationUnsecured = builder.remittanceInformationUnsecured;
        this.requestedExecutionDate = builder.requestedExecutionDate;
        this.phoneNumber = builder.phoneNumber;
        this.email = builder.email;
        this.transactionText = builder.transactionText;
        this.paymentReason = builder.paymentReason;
        this.priority = builder.priority;
    }

    public static class Builder {
        private AccountEntity debtorAccount;
        private InstructedAmountEntity instructedAmount;
        private AccountEntity creditorAccount;
        private String creditorAgent;
        private String creditorName;
        private String creditorAddress;
        private String chargeBearer;
        private String remittanceInformationUnsecured;
        private String requestedExecutionDate;
        private String phoneNumber;
        private String email;
        private String transactionText;
        private String paymentReason;
        private String priority;

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

        public Builder withChargeBearer(String chargeBearer) {
            this.chargeBearer = chargeBearer;
            return this;
        }

        public Builder withRemittanceInformationUnsecured(String remittanceInformationUnsecured) {
            this.remittanceInformationUnsecured = remittanceInformationUnsecured;
            return this;
        }

        public Builder withRequestedExecutionDate(String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public Builder withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withTransactionText(String transactionText) {
            this.transactionText = transactionText;
            return this;
        }

        public Builder withPaymentReason(String paymentReason) {
            this.paymentReason = paymentReason;
            return this;
        }

        public Builder withPriority(String priority) {
            this.priority = priority;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
