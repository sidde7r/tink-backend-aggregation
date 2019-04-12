package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {
    private double amount;
    private CreditorEntity creditor;
    private String currency;
    private DebtorEntity debtor;

    @JsonProperty("external_id")
    private String externalId;

    private String urgency;

    private CreatePaymentRequest(Builder builder) {
        this.amount = builder.amount;
        this.creditor = builder.creditor;
        this.currency = builder.currency;
        this.debtor = builder.debtor;
        this.externalId = builder.externalId;
        this.urgency = builder.urgency;
    }

    public static class Builder {
        private double amount;
        private CreditorEntity creditor;
        private String currency;
        private DebtorEntity debtor;
        private String externalId;
        private String urgency;

        public Builder withAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder withCreditor(CreditorEntity creditor) {
            this.creditor = creditor;
            return this;
        }

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder withDebtor(DebtorEntity debtor) {
            this.debtor = debtor;
            return this;
        }

        public Builder withExternalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public Builder withUrgency(String urgency) {
            this.urgency = urgency;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
