package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities.PaymentRecurrence;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CreatePaymentRequest {
    private double amount;
    private CreditorEntity creditor;
    private String currency;
    private DebtorEntity debtor;

    @JsonProperty("external_id")
    private String externalId;

    private String urgency;

    @JsonProperty("requested_execution_date")
    private String executionDate;

    @JsonProperty("recurring")
    private PaymentRecurrence recurrence;

    private CreatePaymentRequest(Builder builder) {
        this.amount = builder.amount;
        this.creditor = builder.creditor;
        this.currency = builder.currency;
        this.debtor = builder.debtor;
        this.externalId = builder.externalId;
        this.urgency = builder.urgency;
        this.executionDate = builder.executionDate;
        this.recurrence = builder.recurrence;
    }

    public static class Builder {

        private String executionDate;
        private double amount;
        private CreditorEntity creditor;
        private String currency;
        private DebtorEntity debtor;
        private String externalId;
        private String urgency;
        private PaymentRecurrence recurrence;

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

        public Builder withExecutionDate(String executionDate) {
            this.executionDate = executionDate;
            return this;
        }

        public Builder withUrgency(String urgency) {
            this.urgency = urgency;
            return this;
        }

        public Builder withRecurrence(PaymentRecurrence recurrence) {
            this.recurrence = recurrence;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
