package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshRequest {
    private final String refreshToken;
    private final String clientId;
    private final String grantType;

    public RefreshRequest(String refreshToken, String clientId, String grantType) {
        this.refreshToken = refreshToken;
        this.clientId = clientId;
        this.grantType = grantType;
    }

    public Object toData() {
        return Form.builder()
                .put(BnpParibasBaseConstants.QueryKeys.CLIENT_ID, clientId)
                .put(BnpParibasBaseConstants.QueryKeys.GRANT_TYPE, grantType)
                .put(BnpParibasBaseConstants.QueryKeys.REFRESH_TOKEN, refreshToken)
                .build()
                .serialize();
    }
}
