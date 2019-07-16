package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AdressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreatePaymentRequest {
    private AmountEntity instructedAmount;
    private String creditorName;
    private CreditorAccountEntity creditorAccount;
    private DebtorAccountEntity debtorAccount;
    private AdressEntity creditorAddress;
    private String ultimateDebtor;
    private String remittanceInformationUnstructured;
    private String requestedExecutionDate;

    public CreatePaymentRequest() {}

    private CreatePaymentRequest(Builder builder) {
        this.instructedAmount = builder.instructedAmount;
        this.creditorName = builder.creditorName;
        this.creditorAccount = builder.creditorAccount;
        this.debtorAccount = builder.debtorAccount;
        this.creditorAddress = builder.creditorAddress;
        this.ultimateDebtor = builder.ultimateDebtor;
        this.remittanceInformationUnstructured = builder.remittanceInformationUnstructured;
        this.remittanceInformationUnstructured = builder.requestedExecutionDate;
    }

    public String toData() {
        return SerializationUtils.serializeToString(this);
    }

    public static class Builder {
        private AmountEntity instructedAmount;
        private String creditorName;
        private CreditorAccountEntity creditorAccount;
        private DebtorAccountEntity debtorAccount;
        private AdressEntity creditorAddress;
        private String ultimateDebtor;
        private String remittanceInformationUnstructured;
        private String requestedExecutionDate;

        public Builder withInstructedAmount(AmountEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
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

        public Builder withCreditorAddress(AdressEntity creditorAddress) {
            this.creditorAddress = creditorAddress;
            return this;
        }

        public Builder withUltimateDebtor(String ultimateDebtor) {
            this.ultimateDebtor = ultimateDebtor;
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
