package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@JsonObject
public class AuthenticateResponse {

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("refresh_expires_in")
    private int refreshExpiresIn;

    @JsonProperty("access_token")
    private String accessToken;

    private String scope;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("issued_token_type")
    private String issuedTokenType;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("agreement_id")
    private int agreementId;

    public String getRefreshToken() {
        return refreshToken;
    }

    public int getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getScope() {
        return scope;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getIssuedTokenType() {
        return issuedTokenType;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getUserId() {
        return userId;
    }

    public int getAgreementId() {
        return agreementId;
    }

    public void storeTokens(SessionStorage storage) {
        storage.put(NordeaFiConstants.SessionStorage.ACCESS_TOKEN, accessToken);
        storage.put(NordeaFiConstants.SessionStorage.REFRESH_TOKEN, refreshToken);
        storage.put(NordeaFiConstants.SessionStorage.TOKEN_TYPE, tokenType);
    }
}
