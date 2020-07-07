package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstructedAmountRequest {

    private final String currency;
    private final String amount;

    @JsonIgnore
    private InstructedAmountRequest(String currency, String amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public static InstructedAmountBuilder builder() {
        return new InstructedAmountBuilder();
    }

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }

    public static class InstructedAmountBuilder {

        private String currency;
        private String amount;

        InstructedAmountBuilder() {}

        public InstructedAmountBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public InstructedAmountBuilder amount(String amount) {
            this.amount = amount;
            return this;
        }

        public InstructedAmountRequest build() {
            return new InstructedAmountRequest(currency, amount);
        }
    }
}
