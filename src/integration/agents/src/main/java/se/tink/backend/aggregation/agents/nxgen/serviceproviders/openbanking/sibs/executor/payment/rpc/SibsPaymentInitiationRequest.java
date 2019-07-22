package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.joda.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonInclude(Include.NON_NULL)
@JsonObject
public class SibsPaymentInitiationRequest {

    private String endToEndIdentification;
    private SibsAccountReferenceEntity debtorAccount;
    private SibsAmountEntity instructedAmount;
    private SibsAccountReferenceEntity creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private SibsAddressEntity creditorAddress;
    private String remittanceInformationUnstructured;
    private String creditorClearingCode;
    private LocalDate requestedExecutionDate;

    public SibsPaymentInitiationRequest(Builder builder) {
        this.endToEndIdentification = builder.endToEndIdentification;
        this.debtorAccount = builder.debtorAccount;
        this.instructedAmount = builder.instructedAmount;
        this.creditorAccount = builder.creditorAccount;
        this.creditorAgent = builder.creditorAgent;
        this.creditorName = builder.creditorName;
        this.creditorAddress = builder.creditorAddress;
        this.remittanceInformationUnstructured = builder.remittanceInformationUnstructured;
        this.creditorClearingCode = builder.creditorClearingCode;
        this.requestedExecutionDate = builder.requestedExecutionDate;
    }

    public String getEndToEndIdentification() {
        return endToEndIdentification;
    }

    public SibsAccountReferenceEntity getDebtorAccount() {
        return debtorAccount;
    }

    public SibsAmountEntity getInstructedAmount() {
        return instructedAmount;
    }

    public SibsAccountReferenceEntity getCreditorAccount() {
        return creditorAccount;
    }

    public String getCreditorAgent() {
        return creditorAgent;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public SibsAddressEntity getCreditorAddress() {
        return creditorAddress;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public String getCreditorClearingCode() {
        return creditorClearingCode;
    }

    public LocalDate getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public static class Builder {
        private String endToEndIdentification;
        private SibsAccountReferenceEntity debtorAccount;
        private SibsAmountEntity instructedAmount;
        private SibsAccountReferenceEntity creditorAccount;
        private String creditorAgent;
        private String creditorName;
        private SibsAddressEntity creditorAddress;
        private String remittanceInformationUnstructured;
        private String creditorClearingCode;
        private LocalDate requestedExecutionDate;

        public Builder withEndToEndIdentification(String endToEndIdentification) {
            this.endToEndIdentification = endToEndIdentification;
            return this;
        }

        public Builder withDebtorAccount(SibsAccountReferenceEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public Builder withInstructedAmount(SibsAmountEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public Builder withCreditorAccount(SibsAccountReferenceEntity creditorAccount) {
            this.creditorAccount = creditorAccount;
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

        public Builder withCreditorAddress(SibsAddressEntity creditorAddress) {
            this.creditorAddress = creditorAddress;
            return this;
        }

        public Builder withRemittanceInformationUnstructured(
                String remittanceInformationUnstructured) {
            this.remittanceInformationUnstructured = remittanceInformationUnstructured;
            return this;
        }

        public Builder withCreditorClearingCode(String creditorClearingCode) {
            this.creditorClearingCode = creditorClearingCode;
            return this;
        }

        public Builder withRequestedExecutionDate(LocalDate requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public SibsPaymentInitiationRequest build() {
            return new SibsPaymentInitiationRequest(this);
        }
    }
}
