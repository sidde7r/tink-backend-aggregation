package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
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
        sessionStorage.put(NordeaSEConstants.StorageKeys.ACCESS_TOKEN, accessToken);
        sessionStorage.put(NordeaSEConstants.StorageKeys.REFRESH_TOKEN, refreshToken);
        sessionStorage.put(NordeaSEConstants.StorageKeys.TOKEN_TYPE, tokenType);
        sessionStorage.put(
                NordeaSEConstants.StorageKeys.TOKEN_AUTH_METHOD, NordeaSEConstants.AuthMethod.NASA);
    }
}
