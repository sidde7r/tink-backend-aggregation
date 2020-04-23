package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenExchangeResponse {

    private String expiresIn;

    private String scope;

    private String accessToken;

    private String tokenType;

    public String getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getBearerToken() {
        return tokenType + " " + accessToken;
    }
}
