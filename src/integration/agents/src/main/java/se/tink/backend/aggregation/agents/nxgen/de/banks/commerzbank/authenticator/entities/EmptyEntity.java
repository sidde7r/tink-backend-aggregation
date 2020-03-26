package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EmptyEntity {

    private EmptyEntity() {}

    public static EmptyEntity create() {
        return new EmptyEntity();
    }
}
