package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.form.Form;

@JsonObject
public class RefreshTokenRequest {
    private final String refreshToken;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public RefreshTokenRequest(
            String refreshToken, String clientId, String clientSecret, String redirectUri) {
        this.refreshToken = refreshToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public String toData() {
        return Form.builder()
                .put(SwedbankConstants.QueryKeys.CLIENT_ID, clientId)
                .put(SwedbankConstants.QueryKeys.CLIENT_SECRET, clientSecret)
                .put(SwedbankConstants.QueryKeys.REDIRECT_URI, redirectUri)
                .put(SwedbankConstants.QueryKeys.REFRESH_TOKEN, refreshToken)
                .put(
                        SwedbankConstants.QueryKeys.GRANT_TYPE,
                        SwedbankConstants.QueryValues.GRANT_TYPE_REFRESH_TOKEN)
                .build()
                .serialize();
    }
}
