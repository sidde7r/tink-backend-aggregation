package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpenSessionRequest extends BelfiusRequest {

    public static Builder create(String locale) {
        return BelfiusRequest.builder()
                .setRequests(
                        se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.OpenSessionRequest
                                .create(locale));
    }
}
