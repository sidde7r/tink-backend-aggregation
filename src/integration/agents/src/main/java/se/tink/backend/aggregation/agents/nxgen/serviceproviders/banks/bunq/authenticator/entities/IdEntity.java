package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdEntity {
    private long id;

    public long getId() {
        return id;
    }
}
