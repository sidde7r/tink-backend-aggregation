package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqResponse;

public class CreateSessionUserResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<CreateSessionUserResponse> response;

    public BunqResponse<CreateSessionUserResponse> getResponse() {
        return response;
    }
}
