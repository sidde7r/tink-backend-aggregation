package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class ClientCredentialsTokenRequest extends AbstractForm {

    public ClientCredentialsTokenRequest(String clientId, String grantType, String scope) {
        put(FormKeys.CLIENT_ID, clientId);
        put(FormKeys.GRANT_TYPE, grantType);
        put(FormKeys.SCOPE, scope);
    }
}
