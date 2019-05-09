package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class RefreshTokenRequest {
    private final String grantType;
    private final String refreshToken;
    private final String redirectUri;

    public RefreshTokenRequest(String grantType, String refreshToken, String redirectUri) {
        this.grantType = grantType;
        this.refreshToken = refreshToken;
        this.redirectUri = redirectUri;
    }

    public Object toData() {
        return Form.builder()
                .put(SBABConstants.QueryKeys.GRANT_TYPE, grantType)
                .put(SBABConstants.QueryKeys.CODE, refreshToken)
                .put(SBABConstants.QueryKeys.REDIRECT_URI, redirectUri)
                .build()
                .serialize();
    }
}
