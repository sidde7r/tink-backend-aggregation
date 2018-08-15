package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class WebLoginResponse {

    private String tokenType;
    private String accessToken;

    public WebLoginResponse() {
    }

    public WebLoginResponse(String tokenType, String accessToken) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
