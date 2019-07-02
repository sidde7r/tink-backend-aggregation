package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class TokenRequest {
    private String grantType;
    private String code;

    public TokenRequest(String grantType, String code) {
        this.grantType = grantType;
        this.code = code;
    }

    public String toData() {
        return Form.builder()
                .put(PayPalConstants.FormKeys.GRANT_TYPE, grantType)
                .put(PayPalConstants.FormKeys.CODE, code)
                .build()
                .serialize();
    }
}
