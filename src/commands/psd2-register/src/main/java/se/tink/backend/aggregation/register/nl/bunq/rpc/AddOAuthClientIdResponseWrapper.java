package se.tink.backend.aggregation.register.nl.bunq.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;

public class AddOAuthClientIdResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<AddOAuthClientIdResponse> response;

    public BunqResponse<AddOAuthClientIdResponse> getResponse() {
        return response;
    }
}
