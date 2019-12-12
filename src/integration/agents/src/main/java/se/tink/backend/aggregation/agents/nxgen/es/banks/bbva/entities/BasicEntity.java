package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BasicEntity {
    private String id;

    public BasicEntity() {}

    public BasicEntity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
