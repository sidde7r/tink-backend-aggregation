package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.Form;

public class TokenRequestPost extends TokenBaseRequest{

    private final String codeVerifier;

    public TokenRequestPost(
            String grantType,
            String code,
            String redirectUri,
            String clientId,
            String clientSecret,
            String codeVerifier) {
        this.grantType = grantType;
        this.code = code;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.codeVerifier = codeVerifier;
    }

    public String toData() {
        return Form.builder()
                .put(FormKeys.GRANT_TYPE, grantType)
                .put(FormKeys.CODE, code)
                .put(FormKeys.REDIRECT_URI, redirectUri)
                .put(FormKeys.CLIENT_ID, clientId)
                .put(FormKeys.CLIENT_SECRET, clientSecret)
                .put(FormKeys.CODE_VERIFIER, codeVerifier)
                .build()
                .serialize();
    }
}
