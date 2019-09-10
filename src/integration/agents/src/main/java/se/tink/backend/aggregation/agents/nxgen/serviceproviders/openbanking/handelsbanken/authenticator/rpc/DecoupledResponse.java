package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class DecoupledResponse {

    private String result;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private String error;

    @JsonCreator
    public DecoupledResponse(
            @JsonProperty("result") String result,
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") int expiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("error") String error) {
        this.result = result;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
        this.error = error;
    }

    public String getResult() {
        return result;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getError() {
        return error;
    }

    public OAuth2Token toOauthToken() {
        return OAuth2Token.create(
                HandelsbankenBaseConstants.QueryKeys.BEARER,
                getAccessToken(),
                getRefreshToken(),
                getExpiresIn());
    }
}
