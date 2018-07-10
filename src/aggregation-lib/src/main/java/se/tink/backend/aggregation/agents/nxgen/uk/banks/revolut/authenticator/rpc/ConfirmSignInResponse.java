package se.tink.backend.aggregation.agents.nxgen.uk.banks.revolut.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.uk.banks.revolut.rpc.BaseUserResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmSignInResponse extends BaseUserResponse {
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }
}
