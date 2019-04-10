package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class CustomerTokenRequest {

    private final String code;
    private final String redirectUri;

    public CustomerTokenRequest(String code, String redirectUri) {
        this.code = code;
        this.redirectUri = redirectUri;
    }

    public String toData() {
        return Form.builder()
                .put(IngConstants.FormKeys.GRANT_TYPE, IngConstants.FormValues.AUTHORIZATION_CODE)
                .put(IngConstants.FormKeys.CODE, code)
                .put(IngConstants.FormKeys.REDIRECT_URI, redirectUri)
                .build()
                .serialize();
    }
}
