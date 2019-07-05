package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.FormKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class RefreshTokenRequest {
    private final String redirectUri;
    private final String grantType;
    private final String refreshToken;

    public RefreshTokenRequest(String redirectUri, String grantType, String refreshToken) {
        this.redirectUri = redirectUri;
        this.grantType = grantType;
        this.refreshToken = refreshToken;
    }

    public String toData() {
        return Form.builder()
                .put(FormKeys.GRANT_TYPE, grantType)
                .put(FormKeys.REDIRECT_URI, redirectUri)
                .put(FormKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }
}
