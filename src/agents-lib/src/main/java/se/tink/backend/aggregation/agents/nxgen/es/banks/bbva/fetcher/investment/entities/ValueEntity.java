package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValueEntity {
    private AmountEntity value;
    private AmountEntity freeAmount;
    private String transactionDate;

    public AmountEntity getValue() {
        return value;
    }

    public AmountEntity getFreeAmount() {
        return freeAmount;
    }
}
