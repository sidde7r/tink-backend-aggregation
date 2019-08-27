package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshTokenRequest {

    private final String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String toData() {
        return Form.builder()
                .put(
                        IngBaseConstants.FormKeys.GRANT_TYPE,
                        IngBaseConstants.FormValues.REFRESH_TOKEN)
                .put(IngBaseConstants.FormKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }
}
