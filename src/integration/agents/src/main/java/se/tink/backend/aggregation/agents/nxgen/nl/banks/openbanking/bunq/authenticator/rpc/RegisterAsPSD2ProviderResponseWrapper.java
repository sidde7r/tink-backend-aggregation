package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqResponse;

public class RegisterAsPSD2ProviderResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<RegisterAsPSD2ProviderResponse> response;

    public BunqResponse<RegisterAsPSD2ProviderResponse> getResponse() {
        return response;
    }
}
