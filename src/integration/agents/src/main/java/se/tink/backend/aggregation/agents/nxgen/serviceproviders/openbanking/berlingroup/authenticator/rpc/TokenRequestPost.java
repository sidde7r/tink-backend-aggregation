package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.form.Form;

public class TokenRequestPost extends TokenBaseRequest {

    private final String clientSecret;

    public TokenRequestPost(
            final String grantType,
            final String code,
            final String redirectUri,
            final String clientId,
            final String clientSecret) {
        this.grantType = grantType;
        this.code = code;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String toData() {
        return Form.builder()
                .put(FormKeys.GRANT_TYPE, grantType)
                .put(FormKeys.CODE, code)
                .put(FormKeys.REDIRECT_URI, redirectUri)
                .put(FormKeys.CLIENT_ID, clientId)
                .put(FormKeys.CLIENT_SECRET, clientSecret)
                .build()
                .serialize();
    }
}
