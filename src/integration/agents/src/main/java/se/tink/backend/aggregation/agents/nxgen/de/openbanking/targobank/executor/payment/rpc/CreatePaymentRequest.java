package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {
    private InstructedAmountEntity instructedAmount;
    private AccountEntity debtorAccount;
    private String creditorName;
    private AccountEntity creditorAccount;
    private String remittanceInformationUnstructured;

    private CreatePaymentRequest(Builder builder) {
        this.debtorAccount = builder.debtorAccount;
        this.instructedAmount = builder.instructedAmount;
        this.creditorAccount = builder.creditorAccount;
        this.creditorName = builder.creditorName;
        this.remittanceInformationUnstructured = builder.remittanceInformationUnstructured;
    }

    public static class Builder {
        private InstructedAmountEntity instructedAmount;
        private AccountEntity debtorAccount;
        private String creditorName;
        private AccountEntity creditorAccount;
        private String remittanceInformationUnstructured;

        public Builder withInstructedAmount(InstructedAmountEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public Builder withDebtorAccount(AccountEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
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

        public Builder withRemittanceInformationUnstructured(
                String remittanceInformationUnstructured) {
            this.remittanceInformationUnstructured = remittanceInformationUnstructured;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
