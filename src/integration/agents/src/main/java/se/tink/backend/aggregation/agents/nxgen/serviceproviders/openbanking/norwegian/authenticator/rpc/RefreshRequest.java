package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.nxgen.http.form.Form;

public class RefreshRequest {
    private final String refreshToken;
    private final String clientId;
    private final String grantType;

    public RefreshRequest(String refreshToken, String clientId, String grantType) {
        this.refreshToken = refreshToken;
        this.clientId = clientId;
        this.grantType = grantType;
    }

    public String toData() {
        return Form.builder()
                .put(NorwegianConstants.QueryKeys.CLIENT_ID, clientId)
                .put(NorwegianConstants.QueryKeys.GRANT_TYPE, grantType)
                .put(NorwegianConstants.QueryKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }
}
