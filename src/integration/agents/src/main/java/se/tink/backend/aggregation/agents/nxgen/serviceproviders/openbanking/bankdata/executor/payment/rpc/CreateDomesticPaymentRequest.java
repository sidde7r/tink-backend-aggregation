package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class CreateDomesticPaymentRequest {

    private CreditorEntity creditorAccount;
    private DebtorEntity debtorAccount;
    private AmountEntity instructedAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String requestedExecutionDate;

    private CreateDomesticPaymentRequest(Builder builder) {
        this.creditorAccount = builder.creditorAccount;
        this.debtorAccount = builder.debtorAccount;
        this.instructedAmount = builder.instructedAmount;
        this.requestedExecutionDate = builder.requestedExecutionDate;
    }

    public String toData() {
        return SerializationUtils.serializeToString(this);
    }

    public static class Builder {
        private CreditorEntity creditorAccount;
        private DebtorEntity debtorAccount;
        private AmountEntity instructedAmount;
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

        public Builder withRequestedExecutionDate(String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
            return this;
        }

        public CreateDomesticPaymentRequest build() {
            return new CreateDomesticPaymentRequest(this);
        }
    }
}
