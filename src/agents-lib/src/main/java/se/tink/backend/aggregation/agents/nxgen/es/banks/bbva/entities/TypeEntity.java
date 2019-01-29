package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TypeEntity {
    private String id;
    private Object name;

    public String getId() {
        return id;
    }
}
