package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshRequest {
    private final String refreshToken;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public RefreshRequest(
            String refreshToken, String clientId, String clientSecret, String redirectUri) {
        this.refreshToken = refreshToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public String toData() {
        return Form.builder()
                .put(SebCommonConstants.QueryKeys.CLIENT_ID, clientId)
                .put(SebCommonConstants.QueryKeys.CLIENT_SECRET, clientSecret)
                .put(SebCommonConstants.QueryKeys.REDIRECT_URI, redirectUri)
                .put(SebCommonConstants.QueryKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }
}
