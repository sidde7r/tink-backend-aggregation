package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqResponse;

public class AddOAuthClientIdResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<AddOAuthClientIdResponse> response;

    public BunqResponse<AddOAuthClientIdResponse> getResponse() {
        return response;
    }
}
