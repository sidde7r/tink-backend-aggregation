package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.SessionOpenedResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpenSessionResponse extends BelfiusResponse {

    public SessionOpenedResponse getSessionData() {
        return filter(SessionOpenedResponse.class).findFirst().orElseThrow(
                () -> new IllegalStateException("Error getting session data"));
    }
}
