package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqResponse;

public class RegisterDeviceResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<RegisterDeviceResponse> response;

    public BunqResponse<RegisterDeviceResponse> getResponse() {
        return response;
    }
}
