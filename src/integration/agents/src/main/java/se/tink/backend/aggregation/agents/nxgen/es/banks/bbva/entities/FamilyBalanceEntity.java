package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FamilyBalanceEntity {
    private BalanceEntity balance;
    private String id;

    public BalanceEntity getBalance() {
        return balance;
    }

    public String getId() {
        return id;
    }
}
