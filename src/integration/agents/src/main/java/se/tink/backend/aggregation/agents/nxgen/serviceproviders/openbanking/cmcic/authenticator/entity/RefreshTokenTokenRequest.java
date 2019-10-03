package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormValues;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class RefreshTokenTokenRequest extends AbstractForm {

    public RefreshTokenTokenRequest(String clientId, String refreshToken) {
        put(FormKeys.CLIENT_ID, clientId);
        put(FormKeys.GRANT_TYPE, FormValues.REFRESH_TOKEN);
        put(FormKeys.REFRESH_TOKEN, refreshToken);
    }
}
