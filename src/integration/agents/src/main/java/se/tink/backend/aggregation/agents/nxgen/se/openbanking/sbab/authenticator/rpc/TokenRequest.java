package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.FormKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class TokenRequest {
    private final String redirectUri;
    private final String code;
    private final String grantType;

    public TokenRequest(String redirectUri, String code, String grantType) {
        this.redirectUri = redirectUri;
        this.code = code;
        this.grantType = grantType;
    }

    public String toData() {
        return Form.builder()
                .put(FormKeys.GRANT_TYPE, grantType)
                .put(FormKeys.REDIRECT_URI, redirectUri)
                .put(FormKeys.CODE, code)
                .build()
                .serialize();
    }
}
