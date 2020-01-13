package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.form.Form;

@JsonObject
public class RefreshTokenRequest {

    private final String grantType;
    private final String refreshToken;

    public RefreshTokenRequest(String grantType, String refreshToken) {
        this.grantType = grantType;
        this.refreshToken = refreshToken;
    }

    public String toData() {
        return Form.builder()
                .put(ArgentaConstants.FormKeys.GRANT_TYPE, grantType)
                .put(ArgentaConstants.FormKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }
}
