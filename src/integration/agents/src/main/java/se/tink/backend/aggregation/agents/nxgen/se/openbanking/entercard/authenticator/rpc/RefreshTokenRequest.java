package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.QueryKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class RefreshTokenRequest {
    private final String scope;
    private final String grantType;
    private final String refreshToken;
    private final String clientId;
    private final String clientSecret;

    public RefreshTokenRequest(
            String scope,
            String grantType,
            String refreshToken,
            String clientId,
            String clientSecret) {
        this.scope = scope;
        this.grantType = grantType;
        this.refreshToken = refreshToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public Object toData() {
        return Form.builder()
                .put(QueryKeys.SCOPE, scope)
                .put(QueryKeys.GRANT_TYPE, grantType)
                .put(QueryKeys.REFRESH_TOKEN, refreshToken)
                .put(QueryKeys.CLIENT_ID, clientId)
                .put(QueryKeys.CLIENT_SECRET, clientSecret)
                .build()
                .serialize();
    }
}
