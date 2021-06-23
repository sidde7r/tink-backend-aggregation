package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities.AddressEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonInclude(Include.NON_NULL)
@JsonObject
public class PaymentInitiationRequest {
    private AmountEntity instructedAmount;
    private AccountReferenceEntity debtorAccount;
    private AccountReferenceEntity creditorAccount;
    private String creditorName;
    private AddressEntity creditorAddress;
    private String remittanceInformationUnstructured;
    private String requestedExecutionDate;

    private PaymentInitiationRequest(Builder builder) {
        this.instructedAmount = builder.instructedAmount;
        this.debtorAccount = builder.debtorAccount;
        this.creditorAccount = builder.creditorAccount;
        this.creditorName = builder.creditorName;
        this.creditorAddress = builder.creditorAddress;
        this.remittanceInformationUnstructured = builder.remittanceInformationUnstructured;
        this.requestedExecutionDate = builder.requestedExecutionDate;
    }

    public static class Builder {

        private AmountEntity instructedAmount;
        private AccountReferenceEntity debtorAccount;
        private AccountReferenceEntity creditorAccount;
        private String creditorName;
        private AddressEntity creditorAddress;
        private String remittanceInformationUnstructured;
        private String requestedExecutionDate;

        public Builder withInstructedAmount(AmountEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public Builder withDebtorAccount(AccountReferenceEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public Builder withCreditorAccount(AccountReferenceEntity creditorAccount) {
            this.creditorAccount = creditorAccount;
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

        public Builder withRemittanceInformationUnstructured(
                String remittanceInformationUnstructured) {
            this.remittanceInformationUnstructured = remittanceInformationUnstructured;
            return this;
        }

        public Builder withRequestedExecutionDate(String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public PaymentInitiationRequest build() {
            return new PaymentInitiationRequest(this);
        }
    }
}
