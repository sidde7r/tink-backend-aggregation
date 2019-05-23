package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;

public class TokenExchangeResponseWrapper {
    @JsonProperty("Response")
    private BunqResponse<TokenExchangeResponse> response;

    public BunqResponse<TokenExchangeResponse> getResponse() {
        return response;
    }
}
