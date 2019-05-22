package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqResponse;

public class InstallResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<InstallResponse> response;

    public BunqResponse<InstallResponse> getResponse() {
        return response;
    }
}
