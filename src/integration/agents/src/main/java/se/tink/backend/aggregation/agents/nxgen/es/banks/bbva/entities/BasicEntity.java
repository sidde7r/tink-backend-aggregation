package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BasicEntity {
    private String id;

    public String getId() {
        return id;
    }
}
