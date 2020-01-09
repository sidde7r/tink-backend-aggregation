package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.FormKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.form.Form;

@JsonObject
public class TokenRequest {
    private String grantType;
    private String code;
    private String redirectUri;
    private String clientId;
    private String clientSecret;

    public TokenRequest(
            String grantType,
            String code,
            String redirectUri,
            String clientId,
            String clientSecret) {
        this.grantType = grantType;
        this.code = code;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String toData() {
        return Form.builder()
                .put(FormKeys.REDIRECT_URI, redirectUri)
                .put(FormKeys.GRANT_TYPE, grantType)
                .put(FormKeys.CODE, code)
                .put(FormKeys.CLIENT_ID, clientId)
                .put(FormKeys.CLIENT_SECRET, clientSecret)
                .build()
                .serialize();
    }
}
