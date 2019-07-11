package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.CreditorAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class CreateSepaPaymentRequest {

    private CreditorEntity creditorAccount;
    private DebtorEntity debtorAccount;
    private AmountEntity instructedAmount;
    private String creditorName;
    private String creditorAgent;
    private CreditorAddressEntity creditorAddress;
    private String chargeBearer;
    private String remittanceInformationUnstructured;
    private String endToEndIdentification;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date requestedExecutionDate;

    private CreateSepaPaymentRequest(Builder builder) {
        this.creditorAccount = builder.creditorAccount;
        this.debtorAccount = builder.debtorAccount;
        this.instructedAmount = builder.instructedAmount;
        this.requestedExecutionDate = builder.requestedExecutionDate;
        this.creditorName = builder.creditorName;
        this.creditorAgent = builder.creditorAgent;
        this.creditorAddress = builder.creditorAddress;
        this.chargeBearer = builder.chargeBearer;
        this.remittanceInformationUnstructured = builder.remittanceInformationUnstructured;
        this.endToEndIdentification = builder.endToEndIdentification;
    }

    public String toData() {
        return SerializationUtils.serializeToString(this);
    }

    public static class Builder {
        private CreditorEntity creditorAccount;
        private DebtorEntity debtorAccount;
        private AmountEntity instructedAmount;
        private Date requestedExecutionDate;
        private String creditorName;
        private String creditorAgent;
        private CreditorAddressEntity creditorAddress;
        private String chargeBearer;
        private String remittanceInformationUnstructured;
        private String endToEndIdentification;

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

        public Builder withRequestedExecutionDate(Date requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public CreateSepaPaymentRequest build() {
            return new CreateSepaPaymentRequest(this);
        }

        public Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public Builder withCreditorAgent(String creditorAgent) {
            this.creditorAgent = creditorAgent;
            return this;
        }

        public Builder withCreditorAddress(CreditorAddressEntity creditorAddress) {
            this.creditorAddress = creditorAddress;
            return this;
        }

        public Builder withChargeBearer(String chargeBearer) {
            this.chargeBearer = chargeBearer;
            return this;
        }

        public Builder withEndToEndIdentification(String endToEndIdentification) {
            this.endToEndIdentification = endToEndIdentification;
            return this;
        }

        public Builder withRemittanceInformationUnstructured(
                String remittanceInformationUnstructured) {
            this.remittanceInformationUnstructured = remittanceInformationUnstructured;
            return this;
        }
    }
}
