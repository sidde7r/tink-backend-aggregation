package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.entities.AddressEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {
    private String endToEndIdentification;
    private AccountEntity debtorAccount;
    private InstructedAmountEntity instructedAmount;
    private AccountEntity creditorAccount;
    private String creditorAgent;
    private String creditorAgentName;
    private String creditorName;
    private AddressEntity creditorAddress;

    private CreatePaymentRequest(Builder builder) {
        this.endToEndIdentification = builder.endToEndIdentification;
        this.debtorAccount = builder.debtorAccount;
        this.instructedAmount = builder.instructedAmount;
        this.creditorAccount = builder.creditorAccount;
        this.creditorAgent = builder.creditorAgent;
        this.creditorAgentName = builder.creditorAgentName;
        this.creditorName = builder.creditorName;
        this.creditorAddress = builder.creditorAddress;
    }

    public static class Builder {
        private String endToEndIdentification;
        private AccountEntity debtorAccount;
        private InstructedAmountEntity instructedAmount;
        private AccountEntity creditorAccount;
        private String creditorAgent;
        private String creditorAgentName;
        private String creditorName;
        private AddressEntity creditorAddress;

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

        public Builder withCreditorAgentName(String creditorAgentName) {
            this.creditorAgentName = creditorAgentName;
            return this;
        }

        public Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public Builder withCreditorAddress(AddressEntity creditorAddress) {
            this.creditorAddress = creditorAddress;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
