package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@JsonObject
public class PasswordTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private String scope;

    @JsonProperty("token_type")
    private String tokenType;

    private String uh;

    public void storeTokens(SessionStorage sessionStorage) {
        sessionStorage.put(NordeaBaseConstants.StorageKeys.ACCESS_TOKEN, accessToken);
        sessionStorage.put(NordeaBaseConstants.StorageKeys.REFRESH_TOKEN, refreshToken);
        sessionStorage.put(NordeaBaseConstants.StorageKeys.TOKEN_TYPE, tokenType);
        sessionStorage.put(
                NordeaBaseConstants.StorageKeys.TOKEN_AUTH_METHOD,
                NordeaBaseConstants.AuthMethod.NASA);
    }
}
