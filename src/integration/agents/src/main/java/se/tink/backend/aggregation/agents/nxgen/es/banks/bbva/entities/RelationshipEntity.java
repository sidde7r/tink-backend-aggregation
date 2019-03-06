package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RelationshipEntity {
    private TypeEntity type;

    public TypeEntity getType() {
        return type;
    }
}
