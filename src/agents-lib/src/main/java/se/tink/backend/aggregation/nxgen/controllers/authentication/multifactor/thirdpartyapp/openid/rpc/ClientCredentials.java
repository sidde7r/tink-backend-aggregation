package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientCredentials {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    private int expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void validate(){
        Preconditions.checkState(!Strings.isNullOrEmpty(accessToken), "Access token myst exist.");
        Preconditions.checkState(!Strings.isNullOrEmpty(tokenType), "Token type must be defined.");
        Preconditions.checkState(expiresIn > 0, "Expiration time must be defined.");
    }
}
