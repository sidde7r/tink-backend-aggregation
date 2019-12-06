package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.FormValues;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshTokenRequest {
    private String refreshToken;
    private String clientId;
    private String clientSecret;

    public RefreshTokenRequest(String refreshToken, String clientId, String clientSecret) {
        this.refreshToken = refreshToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String toData() {
        return Form.builder()
                .put(FormKeys.CLIENT_ID, clientId)
                .put(FormKeys.CLIENT_SECRET, clientSecret)
                .put(FormKeys.GRANT_TYPE, FormValues.REFRESH_TOKEN)
                .put(FormKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }
}
