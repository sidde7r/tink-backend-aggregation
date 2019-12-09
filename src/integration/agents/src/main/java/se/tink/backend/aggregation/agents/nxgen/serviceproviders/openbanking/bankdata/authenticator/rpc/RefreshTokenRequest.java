package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.QueryKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class RefreshTokenRequest implements TokenRequest {
    private final String grantType;
    private final String refreshToken;
    private final String redirectUri;
    private final String clientId;
    private final String codeVerifier;

    public RefreshTokenRequest(
            String grantType,
            String refreshToken,
            String redirectUri,
            String clientId,
            String codeVerifier) {
        this.grantType = grantType;
        this.refreshToken = refreshToken;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.codeVerifier = codeVerifier;
    }

    public String toData() {
        return Form.builder()
                .put(QueryKeys.GRANT_TYPE, grantType)
                .put(QueryKeys.REFRESH_TOKEN, refreshToken)
                .put(QueryKeys.REDIRECT_URI, redirectUri)
                .put(QueryKeys.CLIENT_ID, clientId)
                .put(QueryKeys.CODE_VERIFIER, codeVerifier)
                .build()
                .serialize();
    }
}
