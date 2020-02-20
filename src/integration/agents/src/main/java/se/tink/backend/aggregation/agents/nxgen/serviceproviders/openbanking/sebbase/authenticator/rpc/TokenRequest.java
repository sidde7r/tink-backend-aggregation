package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.QueryKeys;
import se.tink.backend.aggregation.nxgen.http.form.Form;

public class TokenRequest {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String grantType;
    private final String code;
    private final String scope;

    public TokenRequest(
            String clientId,
            String clientSecret,
            String redirectUri,
            String grantType,
            String code,
            String scope) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.grantType = grantType;
        this.code = code;
        this.scope = scope;
    }

    public String toData() {
        return Form.builder()
                .put(QueryKeys.CLIENT_ID, clientId)
                .put(QueryKeys.CLIENT_SECRET, clientSecret)
                .put(QueryKeys.REDIRECT_URI, redirectUri)
                .put(QueryKeys.GRANT_TYPE, grantType)
                .put(QueryKeys.CODE, code)
                .put(QueryKeys.SCOPE, scope)
                .build()
                .serialize();
    }
}
