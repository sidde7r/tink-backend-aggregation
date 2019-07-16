package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class ClientCredentialsTokenRequest {
    private String grantType;

    public ClientCredentialsTokenRequest(String grantType) {
        this.grantType = grantType;
    }

    public String toData() {
        return Form.builder()
                .put(PayPalConstants.FormKeys.GRANT_TYPE, grantType)
                .build()
                .serialize();
    }
}
