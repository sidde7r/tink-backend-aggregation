package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SEBConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class TokenRequest {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String code;
    private final String grantType;
    private final String scope;

    public TokenRequest(String clientId, String clientSecret, String redirectUri, String code, String grantType,
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
                .put(SEBConstants.QueryKeys.CLIENT_ID, clientId)
                .put(SEBConstants.QueryKeys.CLIENT_SECRET, clientSecret)
                .put(SEBConstants.QueryKeys.REDIRECT_URI, redirectUri)
                .put(SEBConstants.QueryKeys.CODE, code)
                .put(SEBConstants.QueryKeys.GRANT_TYPE, grantType)
                .put(SEBConstants.QueryKeys.SCOPE, scope)
                .build()
                .serialize();
    }
}
