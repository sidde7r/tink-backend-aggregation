package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.FormKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class TokenRequest {
    private final String grantType;
    private final String code;
    private final String redirectUri;
    private final String clientId;
    private final String clientSecret;

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
                .put(FormKeys.GRANT_TYPE, grantType)
                .put(FormKeys.CODE, code)
                .put(FormKeys.REDIRECT_URI, redirectUri)
                .put(FormKeys.CLIENT_ID, clientId)
                .put(FormKeys.CLIENT_SECRET, clientSecret)
                .build()
                .serialize();
    }
}
