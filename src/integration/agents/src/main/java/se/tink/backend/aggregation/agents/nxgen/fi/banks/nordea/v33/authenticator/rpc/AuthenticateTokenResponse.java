package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class AuthenticateTokenResponse {
    private String refreshToken;
    private int refreshExpiresIn;
    private String accessToken;
    private String scope;
    private int expiresIn;
    private String issuedTokenType;
    private String tokenType;
    private String userId;
    private int agreementId;

    public void storeTokens(SessionStorage storage) {
        storage.put(NordeaFIConstants.SessionStorage.ACCESS_TOKEN, accessToken);
        storage.put(NordeaFIConstants.SessionStorage.REFRESH_TOKEN, refreshToken);
        storage.put(NordeaFIConstants.SessionStorage.TOKEN_TYPE, tokenType);
    }
}
