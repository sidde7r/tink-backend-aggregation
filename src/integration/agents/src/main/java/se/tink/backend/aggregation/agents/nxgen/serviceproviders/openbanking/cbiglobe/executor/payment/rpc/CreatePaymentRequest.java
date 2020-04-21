package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.RemittanceInformationStructuredEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {

    private AccountEntity debtorAccount;
    private InstructedAmountEntity instructedAmount;
    private AccountEntity creditorAccount;
    private String creditorName;
    private String transactionType;
    private String remittanceInformationUnstructured;
    private RemittanceInformationStructuredEntity remittanceInformationStructured;
    private String requestedExecutionDate;
    private String requestedExecutionTime;

    @JsonIgnore
    private CreatePaymentRequest(Builder builder) {
        this.debtorAccount = builder.debtorAccount;
        this.instructedAmount = builder.instructedAmount;
        this.creditorAccount = builder.creditorAccount;
        this.creditorName = builder.creditorName;
        this.transactionType = builder.transactionType;
        this.remittanceInformationUnstructured = builder.remittanceInformationUnstructured;
        this.remittanceInformationStructured = builder.remittanceInformationStructured;
        this.requestedExecutionDate = builder.requestedExecutionDate;
        this.requestedExecutionTime = builder.requestedExecutionTime;
    }

    public static class Builder {

        private AccountEntity debtorAccount;
        private InstructedAmountEntity instructedAmount;
        private AccountEntity creditorAccount;
        private String creditorName;
        private String transactionType;
        private String remittanceInformationUnstructured;
        private RemittanceInformationStructuredEntity remittanceInformationStructured;
        private String requestedExecutionDate;
        private String requestedExecutionTime;

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

        public Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public Builder withTransactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public Builder withRemittanceInformationUnstructured(
                String remittanceInformationUnstructured) {
            this.remittanceInformationUnstructured = remittanceInformationUnstructured;
            return this;
        }

        public Builder withRemittanceInformationStructured(
                RemittanceInformationStructuredEntity remittanceInformationStructured) {
            this.remittanceInformationStructured = remittanceInformationStructured;
            return this;
        }

        public Builder withRequestedExecutionDate(String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public Builder withRequestedExecutionTime(String requestedExecutionTime) {
            this.requestedExecutionTime = requestedExecutionTime;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
