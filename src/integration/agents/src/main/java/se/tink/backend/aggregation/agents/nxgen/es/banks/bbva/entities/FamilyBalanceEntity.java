package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FamilyBalanceEntity {
    private AmountEntity balance;
    private String id;

    public AmountEntity getBalance() {
        return balance;
    }

    public String getId() {
        return id;
    }
}
