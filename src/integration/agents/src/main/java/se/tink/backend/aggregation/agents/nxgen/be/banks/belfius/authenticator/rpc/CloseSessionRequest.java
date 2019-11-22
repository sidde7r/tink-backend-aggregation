package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.entities.CloseSession;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CloseSessionRequest extends BelfiusRequest {

    public static Builder create(final String sessionId) {
        return BelfiusRequest.builder().setRequests(CloseSession.create(sessionId));
    }
}
