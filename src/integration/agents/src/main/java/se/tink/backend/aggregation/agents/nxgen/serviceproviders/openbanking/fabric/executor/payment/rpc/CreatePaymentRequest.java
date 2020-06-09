package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {

    private AccountEntity debtorAccount;
    private InstructedAmountEntity instructedAmount;
    private AccountEntity creditorAccount;
    private String creditorName;
    private String remittanceInformationUnstructured;

    @JsonIgnore
    private CreatePaymentRequest(Builder builder) {
        this.debtorAccount = builder.debtorAccount;
        this.instructedAmount = builder.instructedAmount;
        this.creditorAccount = builder.creditorAccount;
        this.creditorName = builder.creditorName;
        this.remittanceInformationUnstructured = builder.remittanceInformationUnstructured;
    }

    public static class Builder {

        private AccountEntity debtorAccount;
        private InstructedAmountEntity instructedAmount;
        private AccountEntity creditorAccount;
        private String creditorName;
        private String remittanceInformationUnstructured;

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
