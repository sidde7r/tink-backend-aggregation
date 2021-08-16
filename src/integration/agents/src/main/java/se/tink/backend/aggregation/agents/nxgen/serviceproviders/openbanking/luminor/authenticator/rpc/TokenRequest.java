package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.QueryKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.form.Form;

@JsonObject
public class TokenRequest {
    private final String clientId;
    private final String code;
    private final String grantType;
    private final String redirectUri;
    private final String realm;
    private final String refreshToken;

    public TokenRequest(
            String clientId,
            String code,
            String grantType,
            String redirectUri,
            String realm,
            String refreshToken) {
        this.code = code;
        this.grantType = grantType;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.realm = realm;
        this.refreshToken = refreshToken;
    }

    public String toTokenData() {
        return Form.builder()
                .put(LuminorConstants.QueryKeys.CODE, code)
                .put(LuminorConstants.QueryKeys.GRANT_TYPE, grantType)
                .put(LuminorConstants.QueryKeys.CLIENT_ID, clientId)
                .put(LuminorConstants.QueryKeys.REDIRECT_URI, redirectUri)
                .put(QueryKeys.REALM, realm)
                .build()
                .serialize();
    }

    public String toRefreshTokenData() {
        return Form.builder()
                .put(LuminorConstants.QueryKeys.GRANT_TYPE, grantType)
                .put(LuminorConstants.QueryKeys.CLIENT_ID, clientId)
                .put(LuminorConstants.QueryKeys.REDIRECT_URI, redirectUri)
                .put(QueryKeys.REALM, realm)
                .put(QueryKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }
}
