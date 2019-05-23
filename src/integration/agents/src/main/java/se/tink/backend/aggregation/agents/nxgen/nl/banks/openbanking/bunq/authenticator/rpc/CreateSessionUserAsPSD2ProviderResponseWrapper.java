package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;

public class CreateSessionUserAsPSD2ProviderResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<CreateSessionUserAsPSD2ProviderResponse> response;

    public BunqResponse<CreateSessionUserAsPSD2ProviderResponse> getResponse() {
        return response;
    }
}
