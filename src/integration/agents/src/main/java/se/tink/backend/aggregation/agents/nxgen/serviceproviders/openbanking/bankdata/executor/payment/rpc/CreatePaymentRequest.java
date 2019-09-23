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
public class CreatePaymentRequest {

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

    private CreatePaymentRequest(CreatePaymentRequest.Builder builder) {
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

    public CreatePaymentRequest() {}

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

        public CreatePaymentRequest.Builder withCreditor(CreditorEntity creditorAmount) {
            this.creditorAccount = creditorAmount;
            return this;
        }

        public CreatePaymentRequest.Builder withDebtor(DebtorEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public CreatePaymentRequest.Builder withAmount(AmountEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public CreatePaymentRequest.Builder withRequestedExecutionDate(
                Date requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }

        public CreatePaymentRequest.Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public CreatePaymentRequest.Builder withCreditorAgent(String creditorAgent) {
            this.creditorAgent = creditorAgent;
            return this;
        }

        public CreatePaymentRequest.Builder withCreditorAddress(
                CreditorAddressEntity creditorAddress) {
            this.creditorAddress = creditorAddress;
            return this;
        }

        public CreatePaymentRequest.Builder withChargeBearer(String chargeBearer) {
            this.chargeBearer = chargeBearer;
            return this;
        }

        public CreatePaymentRequest.Builder withEndToEndIdentification(
                String endToEndIdentification) {
            this.endToEndIdentification = endToEndIdentification;
            return this;
        }

        public CreatePaymentRequest.Builder withRemittanceInformationUnstructured(
                String remittanceInformationUnstructured) {
            this.remittanceInformationUnstructured = remittanceInformationUnstructured;
            return this;
        }
    }
}
