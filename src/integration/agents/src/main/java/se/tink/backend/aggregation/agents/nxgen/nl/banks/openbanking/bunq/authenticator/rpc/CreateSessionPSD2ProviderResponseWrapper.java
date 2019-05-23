package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;

public class CreateSessionPSD2ProviderResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<CreateSessionPSD2ProviderResponse> response;

    public BunqResponse<CreateSessionPSD2ProviderResponse> getResponse() {
        return response;
    }
}
