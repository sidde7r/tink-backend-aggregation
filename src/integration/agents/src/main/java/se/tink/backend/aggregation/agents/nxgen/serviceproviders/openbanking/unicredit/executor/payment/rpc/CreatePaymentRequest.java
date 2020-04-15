package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class CreatePaymentRequest {

    private AccountEntity debtorAccount;
    private AccountEntity creditorAccount;
    private AmountEntity instructedAmount;
    private String creditorName;
    private String remittanceInformationUnstructured;
    // When the bank decide to follow the doc, remove the -"
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "CET")
    private Date requestedExecutionDate;

    private CreatePaymentRequest(Builder builder) {
        this.debtorAccount = builder.debtorAccount;
        this.creditorAccount = builder.creditorAccount;
        this.instructedAmount = builder.instructedAmount;
        this.creditorName = builder.creditorName;
        this.remittanceInformationUnstructured = builder.remittanceInformationUnstructured;
        this.requestedExecutionDate = builder.requestedExecutionDate;
    }

    public String toData() {
        return SerializationUtils.serializeToString(this);
    }

    public static class Builder {
        private AccountEntity debtorAccount;
        private AccountEntity creditorAccount;
        private AmountEntity instructedAmount;
        private String creditorName;
        private String remittanceInformationUnstructured;

        private Date requestedExecutionDate;

        public Builder withCreditor(AccountEntity creditorAmount) {
            this.creditorAccount = creditorAmount;
            return this;
        }

        public Builder withDebtor(AccountEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public Builder withAmount(AmountEntity instructedAmount) {
            this.instructedAmount = instructedAmount;
            return this;
        }

        public Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public Builder withUnstructuredRemittance(String remittanceInformationUnstructured) {
            this.remittanceInformationUnstructured = remittanceInformationUnstructured;
            return this;
        }

        public Builder withRequestedExecutionDate(Date requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
