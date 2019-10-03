package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class AuthorizationCodeTokenRequest extends AbstractForm {

    public AuthorizationCodeTokenRequest(
            String clientId, String grantType, String code, String codeVerifier) {

        put(FormKeys.GRANT_TYPE, grantType);
        put(FormKeys.CLIENT_ID, clientId);
        put(FormKeys.CODE, code);
        put(FormKeys.CODE_VERIFIER, codeVerifier);
    }
}
