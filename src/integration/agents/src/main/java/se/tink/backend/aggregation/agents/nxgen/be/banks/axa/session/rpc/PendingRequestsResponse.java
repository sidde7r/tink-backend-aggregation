package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.session.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.OutputEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class PendingRequestsResponse {
    private OutputEntity output;
}
