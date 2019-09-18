package se.tink.backend.aggregation.agents.banks.sbab.executor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountHolderEntity {
    private String partId;
    private String orgPersNbr;
    private int type;
}
