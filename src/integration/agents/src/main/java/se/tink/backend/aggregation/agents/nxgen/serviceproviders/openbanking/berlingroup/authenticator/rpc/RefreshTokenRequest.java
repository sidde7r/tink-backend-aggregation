package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshTokenRequest extends RefreshTokenBaseRequest {

    public RefreshTokenRequest(
            String grantType, String token, String clientId, String clientSecret) {
        super(grantType, token, clientId, clientSecret);
    }

    public String toData() {
        {
            return Form.builder()
                    .put(FormKeys.GRANT_TYPE, grantType)
                    .put(FormKeys.REFRESH_TOKEN, token)
                    .put(FormKeys.CLIENT_ID, clientId)
                    .put(FormKeys.CLIENT_SECRET, clientSecret)
                    .build()
                    .serialize();
        }
    }
}
