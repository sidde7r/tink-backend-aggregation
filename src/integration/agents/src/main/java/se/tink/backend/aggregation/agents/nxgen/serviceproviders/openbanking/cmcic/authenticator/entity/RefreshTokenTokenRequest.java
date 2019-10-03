package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class RefreshTokenTokenRequest extends AbstractForm {

    public RefreshTokenTokenRequest(String clientId, String refreshToken, String grantType) {
        put(FormKeys.CLIENT_ID, clientId);
        put(FormKeys.REFRESH_TOKEN, grantType);
        put(FormKeys.REFRESH_TOKEN, refreshToken);
    }
}
