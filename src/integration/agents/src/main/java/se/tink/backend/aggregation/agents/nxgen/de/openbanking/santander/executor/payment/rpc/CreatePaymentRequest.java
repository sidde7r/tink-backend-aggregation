package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class CreatePaymentRequest {

    private String endToEndIdentification;
    private DebtorEntity debtorAccount;
    private CreditorEntity creditorAccount;
    private AmountEntity instructedAmount;
    private String creditorAgent;
    private String creditorName;
    private String remittanceInformationUnstructured;
    private String requestedExecutionDate;

    @JsonIgnore
    private CreatePaymentRequest(Builder builder) {
        this.endToEndIdentification = builder.endToEndIdentification;
        this.debtorAccount = builder.debtorAccount;
        this.creditorAccount = builder.creditorAccount;
        this.instructedAmount = builder.instructedAmount;
        this.creditorAgent = builder.creditorAgent;
        this.creditorName = builder.creditorName;
        this.requestedExecutionDate = builder.requestedExecutionDate;
        this.remittanceInformationUnstructured = builder.remittanceInformationUnstructured;
    }

    public String toData() {
        return SerializationUtils.serializeToString(this);
    }

    public static class Builder {
        private String endToEndIdentification;
        private DebtorEntity debtorAccount;
        private CreditorEntity creditorAccount;
        private AmountEntity instructedAmount;
        private String creditorAgent;
        private String creditorName;
        private String remittanceInformationUnstructured;
        private String requestedExecutionDate;

        public Builder withCreditor(CreditorEntity creditorAmount) {
            this.creditorAccount = creditorAmount;
            return this;
        }

        public Builder withDebtor(DebtorEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public Builder withAmount(AmountEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public Builder withEndToEndIdentification(String endToEndIdentification) {
            this.endToEndIdentification = endToEndIdentification;
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

        public Builder withRemittanceInformationUnstructured(
                String remittanceInformationUnstructured) {
            this.remittanceInformationUnstructured = remittanceInformationUnstructured;
            return this;
        }

        public Builder withRequestedExecutionDate(String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
