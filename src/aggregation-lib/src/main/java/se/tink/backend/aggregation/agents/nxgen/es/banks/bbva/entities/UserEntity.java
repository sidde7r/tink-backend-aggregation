package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserEntity {
    private String id;

    public UserEntity() {
    }

    public UserEntity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
