package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
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
                .put(
                        IngBaseConstants.FormKeys.GRANT_TYPE,
                        IngBaseConstants.FormValues.AUTHORIZATION_CODE)
                .put(IngBaseConstants.FormKeys.CODE, code)
                .put(IngBaseConstants.FormKeys.REDIRECT_URI, redirectUri)
                .build()
                .serialize();
    }
}
