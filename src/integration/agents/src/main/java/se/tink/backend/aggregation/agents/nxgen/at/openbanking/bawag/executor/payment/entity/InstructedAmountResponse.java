package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstructedAmountResponse {

    private String currency;
    private Double amount;

    public InstructedAmountResponse() {}

    public String getCurrency() {
        return currency;
    }

    public Double getAmount() {
        return amount;
    }
}
