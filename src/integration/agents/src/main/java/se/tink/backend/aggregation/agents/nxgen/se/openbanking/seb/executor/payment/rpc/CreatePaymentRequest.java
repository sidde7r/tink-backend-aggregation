package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {
    private String templateId;
    private AmountEntity instructedAmount;
    private DebtorAccountEntity debtorAccount;
    private CreditorAccountEntity creditorAccount;
    private String debtorAccountMessage;
    private String creditorAccountMessage;
    private String remittanceInformationUnstructured;
    private String creditorName;
    private String requestedExecutionDate;

    @JsonIgnore
    private CreatePaymentRequest(Builder builder) {
        this.templateId = builder.templateId;
        this.instructedAmount = builder.instructedAmount;
        this.debtorAccount = builder.debtorAccount;
        this.debtorAccountMessage = builder.debtorAccountMessage;
        this.creditorAccountMessage = builder.creditorAccountMessage;
        this.requestedExecutionDate = builder.requestedExecutionDate;
        this.remittanceInformationUnstructured = builder.remittanceInformationUnstructured;
        this.creditorAccount = builder.creditorAccount;
        this.creditorName = builder.creditorName;
    }

    public static class Builder {
        private String templateId;
        private AmountEntity instructedAmount;
        private DebtorAccountEntity debtorAccount;
        private CreditorAccountEntity creditorAccount;
        private String debtorAccountMessage;
        private String creditorAccountMessage;
        private String remittanceInformationUnstructured;
        private String creditorName;
        private String requestedExecutionDate;

        public Builder withTemplateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder withAmount(AmountEntity amount) {
            this.instructedAmount = amount;
            return this;
        }

        public Builder withCreditorAccount(CreditorAccountEntity creditorAccount) {
            this.creditorAccount = creditorAccount;
            return this;
        }

        public Builder withDebtorAccount(DebtorAccountEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public Builder withCreditorAccountMessage(String creditorAccountMessage) {
            this.creditorAccountMessage = creditorAccountMessage;
            return this;
        }

        public Builder withDebtorrAccountMessage(String debtorAccountMessage) {
            this.debtorAccountMessage = debtorAccountMessage;
            return this;
        }

        public Builder withExecutionDate(String executionDate) {
            this.requestedExecutionDate = executionDate;
            return this;
        }

        public Builder withRemittanceInformationUnstructured(
                String remittanceInformationUnstructured) {
            this.remittanceInformationUnstructured = remittanceInformationUnstructured;
            return this;
        }

        public Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
