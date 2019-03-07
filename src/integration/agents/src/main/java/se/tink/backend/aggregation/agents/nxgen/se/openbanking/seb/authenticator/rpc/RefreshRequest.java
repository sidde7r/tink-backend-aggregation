package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SEBConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshRequest {
    private final String refreshToken;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public RefreshRequest(String refreshToken, String clientId, String clientSecret, String redirectUri) {
        this.refreshToken = refreshToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public Object toData() {
        return Form.builder()
                .put(SEBConstants.QueryKeys.CLIENT_ID, clientId)
                .put(SEBConstants.QueryKeys.CLIENT_SECRET, clientSecret)
                .put(SEBConstants.QueryKeys.REDIRECT_URI, redirectUri)
                .put(SEBConstants.QueryKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }
}
