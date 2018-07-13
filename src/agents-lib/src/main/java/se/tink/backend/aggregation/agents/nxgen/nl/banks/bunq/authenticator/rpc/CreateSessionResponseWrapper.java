package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqResponse;

public class CreateSessionResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<CreateSessionResponse> response;

    public BunqResponse<CreateSessionResponse> getResponse() {
        return response;
    }
}
