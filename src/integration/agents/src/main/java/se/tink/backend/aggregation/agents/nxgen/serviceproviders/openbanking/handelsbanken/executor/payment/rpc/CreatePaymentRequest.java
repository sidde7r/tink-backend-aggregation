package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {

    @JsonProperty("creditorAccount")
    private AccountEntity creditor;

    @JsonProperty("debtorAccount")
    private AccountEntity debtor;

    @JsonProperty("instructedAmount")
    private AmountEntity amount;

    private CreatePaymentRequest(Builder builder) {
        this.creditor = builder.creditor;
        this.debtor = builder.debtor;
        this.amount = builder.amount;
    }

    public static class Builder {
        private AccountEntity creditor;
        private AccountEntity debtor;
        private AmountEntity amount;

        public Builder withCreditor(AccountEntity creditor) {
            this.creditor = creditor;
            return this;
        }

        public Builder withDebtor(AccountEntity debtor) {
            this.debtor = debtor;
            return this;
        }

        public Builder withAmount(AmountEntity amount) {
            this.amount = amount;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
