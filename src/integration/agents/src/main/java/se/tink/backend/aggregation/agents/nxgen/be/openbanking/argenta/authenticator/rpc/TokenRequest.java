package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class TokenRequest {
    private final String code;
    private final String grantType;
    private final String clientId;
    private final String redirectUri;
    private final String codeVerifier;

    public TokenRequest(
            String code,
            String grantType,
            String clientId,
            String redirectUri,
            String codeVerifier) {
        this.code = code;
        this.grantType = grantType;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.codeVerifier = codeVerifier;
    }

    public String toData() {
        return Form.builder()
                .put(ArgentaConstants.FormKeys.CODE, code)
                .put(ArgentaConstants.FormKeys.GRANT_TYPE, grantType)
                .put(ArgentaConstants.FormKeys.CLIENT_ID, clientId)
                .put(ArgentaConstants.FormKeys.REDIRECT_URI, redirectUri)
                .put(ArgentaConstants.FormKeys.CODE_VERIFIER, codeVerifier)
                .build()
                .serialize();
    }
}
