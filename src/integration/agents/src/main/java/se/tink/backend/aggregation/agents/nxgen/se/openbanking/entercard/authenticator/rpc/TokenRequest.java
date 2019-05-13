package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.QueryKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class TokenRequest {
    private final String scope;
    private final String code;
    private final String grantType;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String codeVerifier;

    public TokenRequest(
            String scope,
            String code,
            String grantType,
            String clientId,
            String clientSecret,
            String redirectUri,
            String codeVerifier) {
        this.scope = scope;
        this.code = code;
        this.grantType = grantType;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.codeVerifier = codeVerifier;
    }

    public String toData() {
        return Form.builder()
                .put(QueryKeys.SCOPE, scope)
                .put(QueryKeys.CODE, code)
                .put(QueryKeys.GRANT_TYPE, grantType)
                .put(QueryKeys.CLIENT_ID, clientId)
                .put(QueryKeys.CLIENT_SECRET, clientSecret)
                .put(QueryKeys.REDIRECT_URI, redirectUri)
                .put(QueryKeys.CODE_VERIFIER, codeVerifier)
                .build()
                .serialize();
    }
}
