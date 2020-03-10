package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseRequest;
import se.tink.backend.aggregation.nxgen.http.form.Form;

public class TokenRequest extends TokenBaseRequest {

    private final String codeVerifier;

    public TokenRequest(
            final String grantType,
            final String code,
            final String redirectUri,
            final String clientId,
            final String codeVerifier) {
        this.grantType = grantType;
        this.code = code;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.codeVerifier = codeVerifier;
    }

    public String toData() {
        return Form.builder()
                .put(FormKeys.GRANT_TYPE, grantType)
                .put(FormKeys.CODE, code)
                .put(FormKeys.REDIRECT_URI, redirectUri)
                .put(FormKeys.CLIENT_ID, clientId)
                .put(FormKeys.CODE_VERIFIER, codeVerifier)
                .build()
                .serialize();
    }
}
