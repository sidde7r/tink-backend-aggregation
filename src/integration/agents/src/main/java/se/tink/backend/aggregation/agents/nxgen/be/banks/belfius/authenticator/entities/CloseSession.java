package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.RequestEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CloseSession extends RequestEntity {

    private String sessionId;

    public static CloseSession create(final String sessionId) {
        final CloseSession entity = new CloseSession();
        entity.sessionId = sessionId;
        return entity;
    }
}
