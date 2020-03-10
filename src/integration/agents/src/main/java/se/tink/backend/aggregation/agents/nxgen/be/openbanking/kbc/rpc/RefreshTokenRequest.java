package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.RefreshTokenBaseRequest;
import se.tink.backend.aggregation.nxgen.http.form.Form;

public class RefreshTokenRequest extends RefreshTokenBaseRequest {

    public RefreshTokenRequest(String grantType, String token, String clientId) {
        super(grantType, token, clientId);
    }

    public String toData() {
        return Form.builder()
                .put(FormKeys.GRANT_TYPE, grantType)
                .put(FormKeys.REFRESH_TOKEN, token)
                .put(FormKeys.CLIENT_ID, clientId)
                .build()
                .serialize();
    }
}
