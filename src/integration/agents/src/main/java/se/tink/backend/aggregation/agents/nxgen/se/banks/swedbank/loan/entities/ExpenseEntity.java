package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExpenseEntity {
    private String description;

    public String getDescription() {
        return description;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    private AmountEntity amount;
}
