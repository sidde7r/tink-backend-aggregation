package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@JsonObject
public class ResultBankIdResponse {
    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("refresh_expires_in")
    private int refreshExpiresIn;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty private String scope;

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

    public void storeTokens(SessionStorage sessionStorage) {
        sessionStorage.put(NordeaSEConstants.StorageKeys.ACCESS_TOKEN, accessToken);
        sessionStorage.put(NordeaSEConstants.StorageKeys.REFRESH_TOKEN, refreshToken);
        sessionStorage.put(NordeaSEConstants.StorageKeys.TOKEN_TYPE, tokenType);
    }
}
