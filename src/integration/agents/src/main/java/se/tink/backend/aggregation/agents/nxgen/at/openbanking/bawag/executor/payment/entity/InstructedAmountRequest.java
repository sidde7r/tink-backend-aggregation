package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstructedAmountRequest extends InstructedAmount {

    private InstructedAmountRequest(String currency, Double amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public static InstructedAmountBuilder builder() {
        return new InstructedAmountBuilder();
    }

    public String getCurrency() {
        return currency;
    }

    public Double getAmount() {
        return amount;
    }

    public static class InstructedAmountBuilder {

        private String currency;
        private Double amount;

        InstructedAmountBuilder() {}

        public InstructedAmountBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public InstructedAmountBuilder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        public InstructedAmountRequest build() {
            return new InstructedAmountRequest(currency, amount);
        }
    }
}
