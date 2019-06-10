package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {
    private DebtorAccountEntity debtorAccount;
    private InstructedAmountEntity instructedAmount;
    private CreditorAccountEntity creditorAccount;
    private String creditorName;

    private CreatePaymentRequest(Builder builder) {
        this.debtorAccount = builder.debtorAccount;
        this.creditorAccount = builder.creditorAccount;
        this.instructedAmount = builder.instructedAmount;
        this.creditorName = builder.creditorName;
    }

    public static class Builder {
        private DebtorAccountEntity debtorAccount;
        private InstructedAmountEntity instructedAmount;
        private CreditorAccountEntity creditorAccount;
        private String creditorName;

        public Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public Builder withDebtorAccount(DebtorAccountEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public Builder withInstructedAmount(InstructedAmountEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public Builder withCreditorAccount(CreditorAccountEntity creditorAccount) {
            this.creditorAccount = creditorAccount;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
