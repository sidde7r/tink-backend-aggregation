package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshRequest {

    private final String grantType;
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;

    public RefreshRequest(
        String grantType, String clientId, String clientSecret, String refreshToken) {
        this.grantType = grantType;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
    }

    public String toData() {
        return Form.builder()
            .put(DeutscheBankConstants.FormKeys.GRANT_TYPE, grantType)
            .put(DeutscheBankConstants.FormKeys.CLIENT_ID, clientId)
            .put(DeutscheBankConstants.FormKeys.CLIENT_SECRET, clientSecret)
            .put(DeutscheBankConstants.FormKeys.REFRESH_TOKEN, refreshToken)
            .build()
            .serialize();
    }
}
