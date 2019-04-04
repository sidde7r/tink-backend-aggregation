package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshTokenRequest {

    private final String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String toData() {
        return Form.builder()
            .put(IngConstants.FormKeys.GRANT_TYPE, IngConstants.FormValues.REFRESH_TOKEN)
            .put(IngConstants.FormKeys.REFRESH_TOKEN, refreshToken)
            .build()
            .serialize();
    }
}
