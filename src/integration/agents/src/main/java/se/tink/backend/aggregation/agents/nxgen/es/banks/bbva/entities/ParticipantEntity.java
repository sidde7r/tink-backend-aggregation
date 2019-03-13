package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ParticipantEntity {
    private int orden;
    private RelationshipEntity relationship;

    public int getOrden() {
        return orden;
    }

    public RelationshipEntity getRelationship() {
        return relationship;
    }
}
