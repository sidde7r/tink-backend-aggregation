package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.nxgen.http.form.Form;

public class TokenRequest {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String code;
    private final String grantType;
    private final String scope;

    public TokenRequest(
            String clientId,
            String clientSecret,
            String redirectUri,
            String code,
            String grantType,
            String scope) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.code = code;
        this.grantType = grantType;
        this.scope = scope;
    }

    public String toData() {
        return Form.builder()
                .put(SebCommonConstants.QueryKeys.CLIENT_ID, clientId)
                .put(SebCommonConstants.QueryKeys.CLIENT_SECRET, clientSecret)
                .put(SebCommonConstants.QueryKeys.REDIRECT_URI, redirectUri)
                .put(SebCommonConstants.QueryKeys.CODE, code)
                .put(SebCommonConstants.QueryKeys.GRANT_TYPE, grantType)
                .put(SebCommonConstants.QueryKeys.SCOPE, scope)
                .build()
                .serialize();
    }
}
