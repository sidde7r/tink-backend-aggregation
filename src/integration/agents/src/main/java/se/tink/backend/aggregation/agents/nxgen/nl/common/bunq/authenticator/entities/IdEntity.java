package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdEntity {
    private long id;

    public long getId() {
        return id;
    }
}
