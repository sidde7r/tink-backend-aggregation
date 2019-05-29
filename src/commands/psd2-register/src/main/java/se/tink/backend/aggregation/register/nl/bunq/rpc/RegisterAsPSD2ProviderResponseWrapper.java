package se.tink.backend.aggregation.register.nl.bunq.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;

public class RegisterAsPSD2ProviderResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<RegisterAsPSD2ProviderResponse> response;

    public BunqResponse<RegisterAsPSD2ProviderResponse> getResponse() {
        return response;
    }
}
