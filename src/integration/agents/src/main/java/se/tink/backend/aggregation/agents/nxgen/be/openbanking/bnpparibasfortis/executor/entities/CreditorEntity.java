package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorEntity {
    private String name;

    CreditorEntity(String name) {
        this.name = name;
    }

    public CreditorEntity() {}
}
