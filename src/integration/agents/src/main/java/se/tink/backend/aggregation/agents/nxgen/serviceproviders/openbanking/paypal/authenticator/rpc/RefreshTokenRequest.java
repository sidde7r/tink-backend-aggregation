package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshTokenRequest {
    private String grantType;
    private String refreshToken;

    public RefreshTokenRequest(String grantType, String refresToken) {
        this.grantType = grantType;
        this.refreshToken = refresToken;
    }

    public String toData() {
        return Form.builder()
                .put(PayPalConstants.FormKeys.GRANT_TYPE, grantType)
                .put(PayPalConstants.FormKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }
}
