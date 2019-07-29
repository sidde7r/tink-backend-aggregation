package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.rpc;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {
    private AmountEntity instructedAmount;
    private AccountEntity debtorAccount;
    private AccountEntity creditorAccount;
    private String creditorName;
    private String remittanceInformationUnstructured;

    private CreatePaymentRequest(Builder builder) {
        this.instructedAmount = builder.instructedAmount;
        this.debtorAccount = builder.debtorAccount;
        this.creditorAccount = builder.creditorAccount;
        this.creditorName = builder.creditorName;
        this.remittanceInformationUnstructured = builder.remittanceInformationUnstructured;
    }

    public static class Builder {
        private AmountEntity instructedAmount;
        private AccountEntity debtorAccount;
        private AccountEntity creditorAccount;
        private String creditorName;
        private String remittanceInformationUnstructured;

        public Builder withInstructedAmount(AmountEntity instructedAmount) {
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
