package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities.ResultBankIdEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.rpc.NordeaSEResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@JsonObject
public class ResultBankIdResponse extends NordeaSEResponse {

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("refresh_expires_in")
    private int refreshExpiresIn;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("id_token")
    private String idToken;

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

    // yes, this is a real field
    private String uh;

    @JsonProperty("getBankIdAuthenticationResultOut")
    private ResultBankIdEntity resultBankIdEntity;

    public String getBankIdStatus() {
        return resultBankIdEntity.getProgressStatus();
    }

    public String getToken() {
        return resultBankIdEntity.getToken();
    }

    public String getId(String orgNumber) {
        return resultBankIdEntity.getId(orgNumber);
    }

    public String getHolderName() {
        return resultBankIdEntity.getHolderName();
    }

    public void storeTokens(SessionStorage sessionStorage) {
        sessionStorage.put(NordeaSEConstants.StorageKeys.ACCESS_TOKEN, accessToken);
        sessionStorage.put(NordeaSEConstants.StorageKeys.REFRESH_TOKEN, refreshToken);
        sessionStorage.put(StorageKeys.ID_TOKEN, idToken);
        sessionStorage.put(NordeaSEConstants.StorageKeys.TOKEN_TYPE, tokenType);
        sessionStorage.put(StorageKeys.UH, uh);
        sessionStorage.put(
                NordeaSEConstants.StorageKeys.TOKEN_AUTH_METHOD,
                NordeaSEConstants.AuthMethod.BANKID_SE);
    }
}
