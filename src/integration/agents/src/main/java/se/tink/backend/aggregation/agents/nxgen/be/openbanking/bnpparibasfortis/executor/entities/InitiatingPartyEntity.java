package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiatingPartyEntity {
    private String name;

    public InitiatingPartyEntity(String name) {
        this.name = name;
    }
}
