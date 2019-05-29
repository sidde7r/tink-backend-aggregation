package se.tink.backend.aggregation.register.nl.bunq.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;

public class RegisterCallbackResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<RegisterCallbackResponse> response;

    public BunqResponse<RegisterCallbackResponse> getResponse() {
        return response;
    }
}
